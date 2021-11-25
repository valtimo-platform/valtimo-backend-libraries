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

package com.ritense.mail.config

import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.model.value.Recipient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import kotlin.streams.toList

@Configuration
@ConfigurationProperties(prefix = "mailing", ignoreUnknownFields = false)
data class MailingProperties(
    var isOnlyAllowWhitelistedRecipients: Boolean = false,
    /**
     * The priority determines the order in which filters will get executed.
     * Filters with a higher priority will get executed later.
     *
     * @return the priority of the WhitelistFilter
     * @see com.ritense.mail.domain.filters.WhitelistFilter
     */
    /**
     * Filters with a higher priority will get executed later.
     */
    var whitelistedPriority: Int = -1,
    var whitelistedEmailAddresses: Collection<String> = emptyList(),
    var whitelistedDomains: Collection<String> = emptyList(),
    var isRedirectAllMails: Boolean = false,
    /**
     * The priority determines the order in which filters will get executed.
     * Filters with a higher priority will get executed later.
     *
     * @return the priority of the RedirectToFilter
     * @see com.ritense.mail.domain.filters.RedirectToFilter
     */
    /**
     * Filters with a higher priority will get executed later.
     */
    var redirectAllMailsPriority: Int = -1,
    var sendRedirectedMailsTo: Collection<Recipient> = emptyList(),


    /**
     * The priority determines the order in which filters will get executed.
     * Filters with a higher priority will get executed later.
     *
     * @return the priority of the BlacklistFilter
     * @see com.ritense.mail.domain.filters.BlacklistFilter
     */
    /**
     * Filters with a higher priority will get executed later.
     */
    var blacklistFilterPriority: Int = 10,
    var isBlacklistFilter: Boolean = true
) {

    fun setSendRedirectedMailsTo(sendRedirectedMailsTo: List<String>) {
        val redirectRecipients = sendRedirectedMailsTo
            .stream()
            .map { mailto: String? -> Recipient.to(EmailAddress.from(mailto), SimpleName.none()) }
            .toList()
        this.sendRedirectedMailsTo = redirectRecipients
    }

}