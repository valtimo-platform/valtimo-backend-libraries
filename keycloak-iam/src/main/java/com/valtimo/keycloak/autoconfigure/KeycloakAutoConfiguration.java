/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation;
import com.ritense.valtimo.contract.security.config.oauth2.NoOAuth2ClientsConfiguredCondition;
import com.valtimo.keycloak.repository.KeycloakCurrentUserRepository;
import com.valtimo.keycloak.security.config.KeycloakOAuth2HttpSecurityConfigurer;
import com.valtimo.keycloak.security.config.ValtimoKeycloakPropertyResolver;
import com.valtimo.keycloak.security.jwt.authentication.KeycloakTokenAuthenticator;
import com.valtimo.keycloak.security.jwt.provider.KeycloakSecretKeyProvider;
import com.valtimo.keycloak.service.KeycloakService;
import com.valtimo.keycloak.service.KeycloakUserManagementService;
import javax.sql.DataSource;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.ClientsConfiguredCondition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;

@AutoConfiguration
@EnableConfigurationProperties(KeycloakSpringBootProperties.class)
public class KeycloakAutoConfiguration {

    @Bean
    @Conditional(NoOAuth2ClientsConfiguredCondition.class)
    @ConditionalOnMissingBean(KeycloakTokenAuthenticator.class)
    public KeycloakTokenAuthenticator keycloakTokenAuthenticator(
        @Value("${valtimo.keycloak.client:}") final String keycloakClient
    ) {
        return new KeycloakTokenAuthenticator(keycloakClient);
    }

    @Bean
    @Conditional(NoOAuth2ClientsConfiguredCondition.class)
    @ConditionalOnMissingBean(KeycloakSecretKeyProvider.class)
    @ConditionalOnProperty("valtimo.oauth.public-key")
    public KeycloakSecretKeyProvider keycloakSecretKeyProvider(
        @Value("${valtimo.oauth.public-key}") final String oauthPublicKey
    ) {
        return new KeycloakSecretKeyProvider(oauthPublicKey);
    }

    @Bean
    @ConditionalOnMissingBean(KeycloakCurrentUserRepository.class)
    public KeycloakCurrentUserRepository keycloakCurrentUserRepository() {
        return new KeycloakCurrentUserRepository();
    }

    @Bean
    @ConditionalOnMissingBean(KeycloakUserManagementService.class)
    @ConditionalOnWebApplication
    public KeycloakUserManagementService keycloakUserManagementService(
        final KeycloakService keycloakService,
        @Value("#{'${spring.security.oauth2.client.registration.keycloakjwt.client-id:${valtimo.keycloak.client:}}'}") final String keycloakClientName
    ) {
        return new KeycloakUserManagementService(keycloakService, keycloakClientName);
    }

    @Bean
    @ConditionalOnMissingBean(KeycloakService.class)
    @DependsOn("valtimoKeycloakPropertyResolver")
    @ConditionalOnWebApplication
    public KeycloakService keycloakService(
            final KeycloakSpringBootProperties properties,
            @Value("#{'${spring.security.oauth2.client.registration.keycloakjwt.client-id:${valtimo.keycloak.client:}}'}") final String keycloakClientName
    ) {
        return new KeycloakService(properties, keycloakClientName);
    }

    @Order(HIGHEST_PRECEDENCE + 31)
    @Bean
    @ConditionalOnClass(DataSource.class)
    @ConditionalOnMissingBean(name = "keycloakLiquibaseMasterChangeLogLocation")
    public LiquibaseMasterChangeLogLocation keycloakLiquibaseMasterChangeLogLocation() {
        return new LiquibaseMasterChangeLogLocation("config/liquibase/keycloak-master.xml");
    }

    @Bean
    @Order(465)
    @ConditionalOnMissingBean(KeycloakOAuth2HttpSecurityConfigurer.class)
    @Conditional(ClientsConfiguredCondition.class)
    public KeycloakOAuth2HttpSecurityConfigurer keycloakOAuth2HttpSecurityConfigurer(
        KeycloakService keycloakService
    ) {
        return new KeycloakOAuth2HttpSecurityConfigurer(
            keycloakService
        );
    }

    @Bean
    @ConditionalOnMissingBean(ValtimoKeycloakPropertyResolver.class)
    public ValtimoKeycloakPropertyResolver valtimoKeycloakPropertyResolver(
        final KeycloakSpringBootProperties properties,
        final ApplicationContext applicationContext
    ) {
        return new ValtimoKeycloakPropertyResolver(properties, applicationContext);
    }

}
