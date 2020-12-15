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

import com.ritense.valtimo.contract.security.jwt.TokenAuthenticator;
import com.ritense.valtimo.contract.security.jwt.provider.SecretKeyProvider;
import com.ritense.valtimo.security.jwt.authentication.TokenAuthenticationService;
import com.ritense.valtimo.security.jwt.provider.SecretKeyResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AuthenticationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TokenAuthenticationService.class)
    public TokenAuthenticationService tokenAuthenticationService(
        final List<TokenAuthenticator> tokenAuthenticators,
        final SecretKeyResolver secretKeyResolver
    ) {
        return new TokenAuthenticationService(tokenAuthenticators, secretKeyResolver);
    }

    @Bean
    @ConditionalOnMissingBean(SecretKeyResolver.class)
    public SecretKeyResolver secretKeyResolver(
        final List<SecretKeyProvider> secretKeyProviders
    ) {
        return new SecretKeyResolver(secretKeyProviders);
    }

}