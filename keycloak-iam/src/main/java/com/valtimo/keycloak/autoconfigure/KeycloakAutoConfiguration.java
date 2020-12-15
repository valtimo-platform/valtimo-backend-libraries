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

package com.valtimo.keycloak.autoconfigure;

import com.valtimo.keycloak.repository.KeycloakCurrentUserRepository;
import com.valtimo.keycloak.security.jwt.authentication.KeycloakTokenAuthenticator;
import com.valtimo.keycloak.security.jwt.provider.KeycloakSecretKeyProvider;
import com.valtimo.keycloak.service.KeycloakUserManagementService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(KeycloakTokenAuthenticator.class)
    public KeycloakTokenAuthenticator keycloakTokenAuthenticator() {
        return new KeycloakTokenAuthenticator();
    }

    @Bean
    @ConditionalOnMissingBean(KeycloakSecretKeyProvider.class)
    @ConditionalOnProperty("valtimo.jwt.secret")
    public KeycloakSecretKeyProvider keycloakSecretKeyProvider(
        @Value("${valtimo.jwt.secret}") final String secret
    ) {
        return new KeycloakSecretKeyProvider(secret);
    }

    @Bean
    @ConditionalOnMissingBean(KeycloakCurrentUserRepository.class)
    public KeycloakCurrentUserRepository keycloakCurrentUserRepository() {
        return new KeycloakCurrentUserRepository();
    }

    @Bean
    @ConditionalOnMissingBean(KeycloakUserManagementService.class)
    @ConditionalOnWebApplication
    public KeycloakUserManagementService keycloakUserManagementService() {
        return new KeycloakUserManagementService();
    }

}