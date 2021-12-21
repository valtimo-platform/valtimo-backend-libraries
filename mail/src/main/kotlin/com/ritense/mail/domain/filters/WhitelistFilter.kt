/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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
import com.ritense.valtimo.contract.mail.model.RawMailMessage
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import java.util.Optional

/**
 * This class consists of two different filters that both perform
 * whitelisting based on email addresses and domains, one for RawMailMessages
 * and one for TemplatedMailMessages.
 *
 * <p>As is the case for all filters, when a message is no longer valid, the
 * message will not be sent.</p>
 *
 * @see MailFilter
 * @see RawMailMessage
 * @see TemplatedMailMessage
 */
class WhitelistFilter(
    private val mailingProperties: MailingProperties
) : MailFilter {

    override fun doFilter(rawMailMessage: RawMailMessage): Optional<RawMailMessage> {
        rawMailMessage.recipients.filterBy {
            (mailingProperties.whitelistedEmailAddresses.contains(it.email.get())
                || mailingProperties.whitelistedDomains.contains(it.email.domain))
        }
        return Optional.of(rawMailMessage)
    }

    override fun doFilter(templatedMailMessage: TemplatedMailMessage): Optional<TemplatedMailMessage> {
        templatedMailMessage.recipients.filterBy {
            (mailingProperties.whitelistedEmailAddresses.contains(it.email.get())
                || mailingProperties.whitelistedDomains.contains(it.email.domain))
        }
        return Optional.of(templatedMailMessage)
    }

    override fun isEnabled(): Boolean {
        return mailingProperties.onlyAllowWhitelistedRecipients
    }

    override fun getPriority(): Int {
        return mailingProperties.whitelistedPriority
    }

}