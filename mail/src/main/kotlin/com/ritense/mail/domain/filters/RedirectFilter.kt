/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.mail.domain.filters

import com.ritense.mail.config.MailingProperties
import com.ritense.valtimo.contract.mail.MailFilter
import com.ritense.valtimo.contract.mail.model.HasRecipients
import com.ritense.valtimo.contract.mail.model.RawMailMessage
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import java.util.Optional

/**
 * This class consists of one filter that redirects all mails to
 * one or more email addresses, instead of sending them to the original
 * email addresses one for RawMailMessages and one for TemplatedMailMessages.
 *
 * <p>As is the case for all filters, when a message is no longer valid, the
 * message will not be sent.</p>
 *
 * @see MailFilter
 * @see RawMailMessage
 * @see TemplatedMailMessage
 */
class RedirectToFilter(
    private val mailingProperties: MailingProperties
) : MailFilter {

    override fun doFilter(rawMailMessage: RawMailMessage): Optional<RawMailMessage> {
        return doFilterInternal(rawMailMessage)
    }

    override fun doFilter(templatedMailMessage: TemplatedMailMessage): Optional<TemplatedMailMessage> {
        return doFilterInternal(templatedMailMessage)
    }

    private fun <T: HasRecipients> doFilterInternal(mailMessage: T): Optional<T> {
        mailMessage
            .recipients
            .filterBy { mailingProperties.sendRedirectedMailsTo.contains(it.email.get()) }

        return if (mailMessage.recipients.isPresent) {
            Optional.of(mailMessage)
        } else {
            Optional.empty()
        }
    }

    override fun isEnabled(): Boolean {
        return mailingProperties.redirectAllMails
    }

    override fun getPriority(): Int {
        return mailingProperties.redirectAllMailsPriority
    }

}