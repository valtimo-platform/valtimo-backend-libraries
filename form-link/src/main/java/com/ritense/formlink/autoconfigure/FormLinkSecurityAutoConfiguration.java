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

package com.ritense.formlink.autoconfigure;

import com.ritense.formlink.security.config.FormAssociationHttpSecurityConfigurer;
import com.ritense.formlink.security.config.FormAssociationManagementHttpSecurityConfigurer;
import com.ritense.formlink.security.config.ProcessLinkHttpSecurityConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Deprecated(since = "10.6.0", forRemoval = true)
@Configuration
public class FormLinkSecurityAutoConfiguration {

    @Order(273)
    @Bean
    @ConditionalOnMissingBean(FormAssociationHttpSecurityConfigurer.class)
    public FormAssociationHttpSecurityConfigurer formAssociationHttpSecurityConfigurer() {
        return new FormAssociationHttpSecurityConfigurer();
    }

    @Order(275)
    @Bean
    @ConditionalOnMissingBean(FormAssociationManagementHttpSecurityConfigurer.class)
    public FormAssociationManagementHttpSecurityConfigurer formAssociationManagementHttpSecurityConfigurer() {
        return new FormAssociationManagementHttpSecurityConfigurer();
    }

    @Order(276)
    @Bean("formProcessLinkHttpSecurityConfigurer")
    @ConditionalOnMissingBean(ProcessLinkHttpSecurityConfigurer.class)
    public ProcessLinkHttpSecurityConfigurer processLinkHttpSecurityConfigurer() {
        return new ProcessLinkHttpSecurityConfigurer();
    }

}
