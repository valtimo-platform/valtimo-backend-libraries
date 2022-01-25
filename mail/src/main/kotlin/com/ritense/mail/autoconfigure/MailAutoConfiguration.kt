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

package com.ritense.mail.autoconfigure

import com.ritense.mail.MailDispatcher
import com.ritense.mail.config.MailingProperties
import com.ritense.mail.domain.filters.BlacklistFilter
import com.ritense.mail.domain.filters.RedirectToFilter
import com.ritense.mail.domain.filters.WhitelistFilter
import com.ritense.mail.repository.BlacklistRepository
import com.ritense.mail.service.BlacklistService
import com.ritense.mail.service.FilteredMailSender
import com.ritense.mail.service.MailService
import com.ritense.valtimo.contract.mail.MailFilter
import com.ritense.valtimo.contract.mail.MailSender
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableConfigurationProperties(value = [MailingProperties::class])
@EnableJpaRepositories(basePackages = ["com.ritense.mail.repository"])
@EntityScan("com.ritense.mail.domain")
class MailAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MailService::class)
    fun mailService(mailSender: MailSender): MailService {
        return MailService(mailSender)
    }

    @Bean
    @ConditionalOnMissingBean(MailSender::class)
    fun filteredMailService(mailDispatcher: MailDispatcher, filters: Collection<MailFilter>): MailSender {
        return FilteredMailSender(mailDispatcher, filters)
    }

    @Bean
    @ConditionalOnMissingBean(BlacklistService::class)
    fun blacklistService(blacklistRepository: BlacklistRepository): BlacklistService {
        return BlacklistService(blacklistRepository)
    }

    //filters
    @Bean
    @ConditionalOnMissingBean(BlacklistFilter::class)
    fun blacklistFilter(
        mailingProperties: MailingProperties,
        blacklistService: BlacklistService
    ): BlacklistFilter {
        return BlacklistFilter(mailingProperties, blacklistService)
    }

    @Bean
    @ConditionalOnMissingBean(RedirectToFilter::class)
    fun redirectToFilter(mailingProperties: MailingProperties): RedirectToFilter {
        return RedirectToFilter(mailingProperties)
    }

    @Bean
    @ConditionalOnMissingBean(WhitelistFilter::class)
    fun whitelistFilter(mailingProperties: MailingProperties): WhitelistFilter {
        return WhitelistFilter(mailingProperties)
    }

}