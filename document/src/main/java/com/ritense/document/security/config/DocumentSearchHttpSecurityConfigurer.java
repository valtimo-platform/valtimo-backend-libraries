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
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException;
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public class DocumentSearchHttpSecurityConfigurer implements HttpSecurityConfigurer {

    private static final String FIELDS_URL = "/api/v1/document-search/{documentDefinitionName}/fields";
    private static final String FIELDS_MANAGEMENT_URL = "/api/management/v1/document-search/{documentDefinitionName}/fields";

    @Override
    public void configure(HttpSecurity http) {
        try {
            http.authorizeHttpRequests(requests ->
                requests.requestMatchers(antMatcher(POST, "/api/v1/document-search")).authenticated()
                    .requestMatchers(antMatcher(POST, "/api/v1/document-definition/{name}/search")).authenticated()
                    .requestMatchers(antMatcher(POST, FIELDS_URL)).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, FIELDS_URL)).authenticated()
                    .requestMatchers(antMatcher(PUT, FIELDS_URL)).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(DELETE, FIELDS_URL)).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, FIELDS_MANAGEMENT_URL)).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(POST, FIELDS_MANAGEMENT_URL)).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(PUT, FIELDS_MANAGEMENT_URL)).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(DELETE, FIELDS_MANAGEMENT_URL)).hasAuthority(ADMIN));
        } catch (Exception e) {
            throw new HttpConfigurerConfigurationException(e);
        }
    }

}
