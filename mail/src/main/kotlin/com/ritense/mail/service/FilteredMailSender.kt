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

class FilteredMailSender(
    private val mailDispatcher: MailDispatcher
    //private val filters: Collection<MailFilter>?
) : MailSender {

    override fun send(rawMailMessage: RawMailMessage): Optional<MutableList<MailMessageStatus>> {
        //TODO apply Filters extract from mandrill module
        return Optional.of(mailDispatcher.send(rawMailMessage))
    }

    override fun send(templatedMailMessage: TemplatedMailMessage): Optional<MutableList<MailMessageStatus>> {
        //TODO apply Filters extract from mandrill module
        return Optional.of(mailDispatcher.send(templatedMailMessage))
    }

    override fun getMaximumSizeAttachments(): Int {
        return mailDispatcher.getMaximumSizeAttachments()
    }

    //TODO FIX
    /*private fun applyFilters(rawMailMessage: RawMailMessage): RawMailMessage? {
        var filteredRawMailMessage = rawMailMessage
        val enabledMailFiltersSortedByPriority = getEnabledMailFiltersSortedByPriority()
        for (mailFilter in enabledMailFiltersSortedByPriority) {
            val optionalFilteredRawMailMessage = mailFilter.doFilter(filteredRawMailMessage)
            filteredRawMailMessage = if (optionalFilteredRawMailMessage.isPresent) {
                optionalFilteredRawMailMessage.get()
            } else {
                return null
            }
        }
        return filteredRawMailMessage
    }

    //TODO FIX
    private fun applyFilters(templatedMailMessage: TemplatedMailMessage): TemplatedMailMessage? {
        var filteredTemplatedMailMessage = templatedMailMessage
        val enabledMailFiltersSortedByPriority = getEnabledMailFiltersSortedByPriority()
        for (mailFilter in enabledMailFiltersSortedByPriority) {
            val optionalFilteredTemplatedMailMessage = mailFilter.doFilter(filteredTemplatedMailMessage)
            filteredTemplatedMailMessage = if (optionalFilteredTemplatedMailMessage.isPresent) {
                optionalFilteredTemplatedMailMessage.get()
            } else {
                return null
            }
        }
        return filteredTemplatedMailMessage
    }

    //TODO FIX
    private fun getEnabledMailFiltersSortedByPriority(): Collection<MailFilter> {
        return filters!!.stream()
            .filter { it.isEnabled }
            .sorted(compareBy { it.priority })
            .toList()
    }*/

}