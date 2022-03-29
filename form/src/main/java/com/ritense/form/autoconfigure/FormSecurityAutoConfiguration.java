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

package com.ritense.form.autoconfigure;

import com.ritense.form.security.config.FormFileHttpSecurityConfigurer;
import com.ritense.form.security.config.FormFileJwtHttpSecurityConfigurer;
import com.ritense.form.security.config.FormHttpSecurityConfigurer;
import com.ritense.form.security.config.FormManagementHttpSecurityConfigurer;
import com.ritense.valtimo.security.jwt.authentication.TokenAuthenticationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class FormSecurityAutoConfiguration {

    @Order(270)
    @Bean
    @ConditionalOnMissingBean(FormHttpSecurityConfigurer.class)
    public FormHttpSecurityConfigurer formHttpSecurityConfigurer() {
        return new FormHttpSecurityConfigurer();
    }

    @Order(270)
    @Bean
    @ConditionalOnMissingBean(FormManagementHttpSecurityConfigurer.class)
    public FormManagementHttpSecurityConfigurer formManagementHttpSecurityConfigurer() {
        return new FormManagementHttpSecurityConfigurer();
    }

    @Order(271)
    @Bean
    @ConditionalOnMissingBean(FormFileHttpSecurityConfigurer.class)
    public FormFileHttpSecurityConfigurer formFileHttpSecurityConfigurer() {
        return new FormFileHttpSecurityConfigurer();
    }

    @Order(272)
    @Bean
    @ConditionalOnMissingBean(FormFileJwtHttpSecurityConfigurer.class)
    public FormFileJwtHttpSecurityConfigurer formFileJwtHttpSecurityConfigurer(
        TokenAuthenticationService tokenAuthenticationService
    ) {
        return new FormFileJwtHttpSecurityConfigurer(tokenAuthenticationService);
    }

}