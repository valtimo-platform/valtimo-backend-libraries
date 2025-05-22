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

package com.ritense.processdocument.security.config;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException;
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public class ProcessDocumentHttpSecurityConfigurer implements HttpSecurityConfigurer {

    private static final String DEFINITION_URL = "/api/v1/process-document/definition";
    private static final String PROCESS_URL = "/api/v1/process-document/demo/{documentDefinitionName}/process";

    @Override
    public void configure(HttpSecurity http) {
        try {
            http.authorizeHttpRequests(requests -> requests
                .requestMatchers(antMatcher(GET, DEFINITION_URL))
                .authenticated()
                .requestMatchers(antMatcher(POST, DEFINITION_URL))
                .hasAuthority(ADMIN)
                .requestMatchers(antMatcher(DELETE, DEFINITION_URL))
                .hasAuthority(ADMIN)
                .requestMatchers(antMatcher(
                    GET, DEFINITION_URL + "/document/{document-definition-name}"))
                .authenticated()
                .requestMatchers(antMatcher(
                    GET, "/api/v2/process-document/definition/document/{document-definition-name}"))
                .authenticated()
                .requestMatchers(antMatcher(
                    GET, "/api/management/v1/process-document/definition/document/{document-definition-name}"))
                .hasAuthority(ADMIN)
                .requestMatchers(antMatcher(
                    GET, DEFINITION_URL + "/document/{document-definition-name}/version/{document-definition-version}")
                )
                .hasAuthority(ADMIN)
                .requestMatchers(antMatcher(
                    GET, DEFINITION_URL + "/process/{process-definition-key}"))
                .hasAuthority(ADMIN)
                .requestMatchers(antMatcher(
                    GET,
                    DEFINITION_URL + "/processinstance/{process-instance-id}"
                ))
                .authenticated()
                .requestMatchers(antMatcher(GET, "/api/v1/process-document/instance/document/{document-id}"))
                .authenticated()
                .requestMatchers(antMatcher(
                    GET, "/api/v1/process-document/instance/document/{document-id}/audit"))
                .authenticated()
                .requestMatchers(antMatcher(
                    POST, "/api/v1/process-document/operation/new-document-and-start-process"))
                .authenticated()
                .requestMatchers(antMatcher(
                    POST, "/api/v1/process-document/operation/modify-document-and-complete-task"))
                .authenticated()
                .requestMatchers(antMatcher(
                    POST, "/api/v1/process-document/operation/modify-document-and-start-process"))
                .authenticated()
                .requestMatchers(antMatcher(
                    GET, PROCESS_URL))
                .hasAuthority(ADMIN)
                .requestMatchers(antMatcher(
                    PUT, PROCESS_URL))
                .hasAuthority(ADMIN)
                .requestMatchers(antMatcher(
                    DELETE, PROCESS_URL))
                .hasAuthority(ADMIN)
                .requestMatchers(antMatcher(
                    POST, "/api/v3/task"))
                .authenticated()
                .requestMatchers(antMatcher(
                    POST, "/api/v1/document-definition/{caseDefinitionName}/task/search"))
                .authenticated()
            );
        } catch (Exception e) {
            throw new HttpConfigurerConfigurationException(e);
        }
    }

}
