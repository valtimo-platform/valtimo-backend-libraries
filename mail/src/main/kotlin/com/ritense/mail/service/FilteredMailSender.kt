/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.mail.service

import com.ritense.mail.MailDispatcher
import com.ritense.valtimo.contract.mail.MailFilter
import com.ritense.valtimo.contract.mail.MailSender
import com.ritense.valtimo.contract.mail.model.MailMessageStatus
import com.ritense.valtimo.contract.mail.model.RawMailMessage
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import java.util.Optional
import kotlin.streams.toList

/**
 * This class is the replacement of the previous MailSender.class
 * In this class filtering is done based on mailfilters.
 * Defacto this is new main class for sending emails.
 */
class FilteredMailSender(
    private val mailDispatcher: MailDispatcher,
    private val filters: Collection<MailFilter>
) : MailSender {

    override fun send(rawMailMessage: RawMailMessage): Optional<List<MailMessageStatus>> {
        val optionalMessage = applyFilters(rawMailMessage)
        if (optionalMessage.isPresent) {
            return Optional.of(mailDispatcher.send(rawMailMessage))
        }
        return Optional.empty()
    }

    override fun send(templatedMailMessage: TemplatedMailMessage): Optional<List<MailMessageStatus>> {
        val optionalMessage = applyFilters(templatedMailMessage)
        if (optionalMessage.isPresent) {
            return Optional.of(mailDispatcher.send(templatedMailMessage))
        }
        return Optional.empty()
    }

    override fun getMaximumSizeAttachments(): Int {
        return mailDispatcher.getMaximumSizeAttachments()
    }

    private fun applyFilters(rawMailMessage: RawMailMessage): Optional<RawMailMessage> {
        var filteredRawMailMessage = rawMailMessage;
        val filters = prioritizedFilters()
        filters.forEach {
            val filteredMailMessageOptional = it.doFilter(filteredRawMailMessage)
            if (filteredMailMessageOptional.isPresent) {
                filteredRawMailMessage = filteredMailMessageOptional.get()
            } else {
                return Optional.empty()
            }
        }
        return Optional.of(filteredRawMailMessage)
    }

    private fun applyFilters(templatedMailMessage: TemplatedMailMessage): Optional<TemplatedMailMessage> {
        var filteredTemplatedMailMessage = templatedMailMessage;
        val filters = prioritizedFilters()
        filters.forEach {
            val filteredMailMessageOptional = it.doFilter(filteredTemplatedMailMessage)
            if (filteredMailMessageOptional.isPresent) {
                filteredTemplatedMailMessage = filteredMailMessageOptional.get()
            } else {
                return Optional.empty()
            }
        }
        return Optional.of(filteredTemplatedMailMessage)
    }

    private fun prioritizedFilters(): Collection<MailFilter> {
        return filters.stream()
            .filter { it.isEnabled }
            .sorted(compareBy { it.priority })
            .toList()
    }

}