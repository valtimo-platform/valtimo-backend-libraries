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

import com.ritense.resource.service.ResourceService;
import com.ritense.valtimo.contract.config.ValtimoProperties;
import com.ritense.valtimo.contract.mail.MailSender;
import com.ritense.valtimo.helper.ActivityHelper;
import com.ritense.valtimo.helper.DelegateTaskHelper;
import com.ritense.valtimo.mail.MailService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class MailAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MailService.class)
    public MailService mailService(
        final MailSender mailSender,
        final DelegateTaskHelper delegateTaskHelper,
        final ValtimoProperties valtimoProperties,
        final ActivityHelper activityHelper,
        final Optional<ResourceService> optionalResourceService
    ) {
        return new MailService(
            mailSender,
            delegateTaskHelper,
            valtimoProperties,
            activityHelper,
            optionalResourceService
        );
    }

}