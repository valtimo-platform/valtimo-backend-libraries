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

package com.ritense.document.security.config;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException;
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public class DocumentDefinitionHttpSecurityConfigurer implements HttpSecurityConfigurer {

    private static final String DEFINITION_URL = "/api/v1/document-definition";

    @Override
    public void configure(HttpSecurity http) {
        try {
            http.authorizeHttpRequests(requests ->
                requests.requestMatchers(antMatcher(GET, DEFINITION_URL))
                    .authenticated()
                    .requestMatchers(antMatcher(GET, DEFINITION_URL))
                    .authenticated()
                    .requestMatchers(antMatcher(POST, "/api/management/v1/document-definition-template"))
                    .hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, DEFINITION_URL + "/{name}"))
                    .authenticated()
                    .requestMatchers(antMatcher(GET, DEFINITION_URL + "/open/count"))
                    .authenticated()
                    .requestMatchers(antMatcher(GET, "/api/management/v1/document-definition"))
                    .hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, "/api/management/v1/document-definition/{name}"))
                    .hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, "/api/management/v1/document-definition/{name}/version/{version}"))
                    .hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, "/api/management/v1/document-definition/{name}/version"))
                    .hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(POST, DEFINITION_URL))
                    .hasAuthority(ADMIN) // Deprecated since v11
                    .requestMatchers(antMatcher(POST, "/api/management/v1/document-definition"))
                    .hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(DELETE, DEFINITION_URL + "/{name}"))
                    .hasAuthority(ADMIN) // Deprecated since v11
                    .requestMatchers(antMatcher(DELETE, "/api/management/v1/document-definition/{name}"))
                    .hasAuthority(ADMIN)
            );
        } catch (Exception e) {
            throw new HttpConfigurerConfigurationException(e);
        }
    }

}