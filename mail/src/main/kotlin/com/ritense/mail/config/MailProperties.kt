/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "mailing", ignoreUnknownFields = false)
data class MailingProperties @ConstructorBinding constructor(
    var onlyAllowWhitelistedRecipients: Boolean = false,
    /**
     * The priority determines the order in which filters will get executed.
     * Filters with a higher priority will get executed later.
     *
     * @return the priority of the WhitelistFilter
     * @see com.ritense.mail.domain.filters.WhitelistFilter
     */
    var whitelistedPriority: Int = -1,
    var whitelistedEmailAddresses: Collection<String> = emptyList(),
    var whitelistedDomains: Collection<String> = emptyList(),
    var redirectAllMails: Boolean = false,
    /**
     * The priority determines the order in which filters will get executed.
     * Filters with a higher priority will get executed later.
     *
     * @return the priority of the RedirectToFilter
     * @see com.ritense.mail.domain.filters.RedirectToFilter
     */
    var redirectAllMailsPriority: Int = -1,
    var sendRedirectedMailsTo: Collection<String> = emptyList(),
    /**
     * The priority determines the order in which filters will get executed.
     * Filters with a higher priority will get executed later.
     *
     * @return the priority of the BlacklistFilter
     * @see com.ritense.mail.domain.filters.BlacklistFilter
     */
    var blacklistFilterPriority: Int = 10,
    var blacklistFilter: Boolean = true
)