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

package com.ritense.valtimo.security.config;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException;
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public class UserHttpSecurityConfigurer implements HttpSecurityConfigurer {

    @Override
    public void configure(HttpSecurity http) {
        try {
            http.authorizeHttpRequests(requests ->
                requests.requestMatchers(antMatcher(GET, "/api/v1/users")).hasAuthority(ADMIN)
                .requestMatchers(antMatcher(POST, "/api/v1/users")).hasAuthority(ADMIN)
                .requestMatchers(antMatcher(PUT, "/api/v1/users")).hasAuthority(ADMIN)
                .requestMatchers(antMatcher(PUT, "/api/v1/users/{userId}/activate")).hasAuthority(ADMIN)
                .requestMatchers(antMatcher(PUT, "/api/v1/users/{userId}/deactivate")).hasAuthority(ADMIN)
                .requestMatchers(antMatcher(GET, "/api/v1/users/email/{email}/")).hasAuthority(ADMIN)
                .requestMatchers(antMatcher(GET, "/api/v1/users/{userId}")).hasAuthority(ADMIN)
                .requestMatchers(antMatcher(GET, "/api/v1/users/authority/{authority}")).hasAuthority(ADMIN)
                .requestMatchers(antMatcher(DELETE, "/api/v1/users/{userId}")).hasAuthority(ADMIN)
                .requestMatchers(antMatcher(POST, "/api/v1/users/send-verification-email/{userId}")).hasAuthority(ADMIN)
                .requestMatchers(antMatcher(GET, "/api/v1/user/settings")).authenticated()
                .requestMatchers(antMatcher(PUT, "/api/v1/user/settings")).authenticated()
            );
        } catch (Exception e) {
            throw new HttpConfigurerConfigurationException(e);
        }
    }

}