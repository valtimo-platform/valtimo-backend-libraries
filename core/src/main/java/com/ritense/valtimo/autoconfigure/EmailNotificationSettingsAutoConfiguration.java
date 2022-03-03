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

package com.ritense.valtimo.autoconfigure;

import com.ritense.valtimo.emailnotificationsettings.repository.EmailNotificationSettingsRepository;
import com.ritense.valtimo.emailnotificationsettings.service.EmailNotificationSettingsService;
import com.ritense.valtimo.emailnotificationsettings.service.impl.EmailNotificationSettingsServiceImpl;
import com.ritense.valtimo.emailnotificationsettings.web.rest.EmailNotificationSettingsResource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan("com.ritense.valtimo.emailnotificationsettings.domain")
@EnableJpaRepositories(basePackageClasses = EmailNotificationSettingsRepository.class)
public class EmailNotificationSettingsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(EmailNotificationSettingsService.class)
    public EmailNotificationSettingsService emailNotificationSettingsService(
        final EmailNotificationSettingsRepository emailNotificationSettingsRepository
    ) {
        return new EmailNotificationSettingsServiceImpl(emailNotificationSettingsRepository);
    }

    @Bean
    @ConditionalOnMissingBean(EmailNotificationSettingsResource.class)
    public EmailNotificationSettingsResource emailNotificationSettingsResource(
        final EmailNotificationSettingsService emailNotificationSettingsService
    ) {
        return new EmailNotificationSettingsResource(emailNotificationSettingsService);
    }

}
