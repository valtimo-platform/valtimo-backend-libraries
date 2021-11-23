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

package com.ritense.mail.autoconfigure;

import com.ritense.mail.MailDispatcher;
import com.ritense.mail.config.MailingProperties;
import com.ritense.mail.config.MandrillProperties;
import com.ritense.mail.domain.filters.BlacklistFilter;
import com.ritense.mail.domain.filters.RedirectToFilter;
import com.ritense.mail.domain.filters.WhitelistFilter;
import com.ritense.mail.repository.BlacklistRepository;
import com.ritense.mail.service.BlacklistService;
import com.ritense.mail.service.MailMessageConverter;
import com.ritense.mail.service.MandrillHealthIndicator;
import com.ritense.mail.service.MandrillMailDispatcher;
import com.ritense.mail.service.WebhookService;
import com.ritense.mail.web.rest.WebhookResource;
import com.ritense.valtimo.contract.mail.MailFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Collection;

@Configuration
@EnableJpaRepositories(basePackages = "com.ritense.mail.repository")
@EntityScan("com.ritense.mail.domain")
@EnableConfigurationProperties(value = {MandrillProperties.class})
public class MandrillMailAutoConfiguration {

    //services
    @Bean
    @ConditionalOnMissingBean(MailMessageConverter.class)
    public MailMessageConverter mailMessageConverter(
        final MandrillProperties mandrillProperties
    ) {
        return new MailMessageConverter(mandrillProperties);
    }

    @Bean
    @ConditionalOnMissingBean(WebhookService.class)
    public WebhookService webhookService(
        final BlacklistService blacklistService,
        final MandrillProperties mandrillProperties
    ) {
        return new WebhookService(blacklistService, mandrillProperties);
    }

    @Bean
    @ConditionalOnMissingBean(MailDispatcher.class)
    public MailDispatcher mailDispatcher(
        final MandrillProperties mandrillProperties,
        final MailMessageConverter mailMessageConverter,
        final Collection<MailFilter> mailFilters
    ) {
        return new MandrillMailDispatcher(mandrillProperties, mailMessageConverter, mailFilters);
    }

    @Bean
    @ConditionalOnMissingBean(MandrillHealthIndicator.class)
    public MandrillHealthIndicator mandrillHealthIndicator(final MandrillProperties mandrillProperties) {
        return new MandrillHealthIndicator(mandrillProperties);
    }

    @Bean
    @ConditionalOnMissingBean(BlacklistService.class)
    public BlacklistService blacklistService(
        final BlacklistRepository blacklistRepository
    ) {
        return new BlacklistService(blacklistRepository);
    }

    //resources

    @Bean
    @ConditionalOnMissingBean(WebhookResource.class)
    public WebhookResource webhookResource(final WebhookService webhookService) {
        return new WebhookResource(webhookService);
    }

    //filters

    @Bean
    @ConditionalOnMissingBean(BlacklistFilter.class)
    public BlacklistFilter blacklistFilter(
        final BlacklistService blacklistService,
        final MailingProperties mailingProperties
    ) {
        return new BlacklistFilter(blacklistService, mailingProperties);
    }

    @Bean
    @ConditionalOnMissingBean(RedirectToFilter.class)
    public RedirectToFilter redirectToFilter(
        final MailingProperties mailingProperties
    ) {
        return new RedirectToFilter(mailingProperties);
    }

    @Bean
    @ConditionalOnMissingBean(WhitelistFilter.class)
    public WhitelistFilter whitelistFilter(
        final MailingProperties mailingProperties
    ) {
        return new WhitelistFilter(mailingProperties);
    }
}