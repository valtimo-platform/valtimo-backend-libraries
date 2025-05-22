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

    private static final String FEATURE_PROCESS_URL =
        "/api/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/feature-process";

    @Override
    public void configure(HttpSecurity http) {
        try {
            http.authorizeHttpRequests(requests -> requests
                .requestMatchers(antMatcher(GET, "/api/v1/case-definition/{caseDefinitionKey}/case-process-link"))
                .authenticated()
                .requestMatchers(antMatcher(GET, "/api/v1/document-instance/{documentId}/case-process-link"))
                .authenticated()
                .requestMatchers(antMatcher(GET, "/api/v1/process-instance/{processInstanceId}/case-process-link"))
                .authenticated()
                .requestMatchers(antMatcher(GET, "/api/v1/process-document/instance/document/{document-id}"))
                .authenticated()
                .requestMatchers(antMatcher(GET, "/api/v1/process-document/instance/document/{document-id}/audit"))
                .authenticated()
                .requestMatchers(antMatcher(POST, "/api/v1/process-document/operation/new-document-and-start-process"))
                .authenticated()
                .requestMatchers(antMatcher(
                    POST,
                    "/api/v1/process-document/operation/modify-document-and-complete-task"
                ))
                .authenticated()
                .requestMatchers(antMatcher(
                    POST,
                    "/api/v1/process-document/operation/modify-document-and-start-process"
                ))
                .authenticated()
                .requestMatchers(antMatcher(POST, "/api/v3/task"))
                .authenticated()
                .requestMatchers(antMatcher(POST, "/api/v1/document-definition/{caseDefinitionName}/task/search"))
                .authenticated()
                //admin endpoints
                .requestMatchers(antMatcher(GET, FEATURE_PROCESS_URL + "/{type}"))
                .hasAuthority(ADMIN)
                .requestMatchers(antMatcher(PUT, FEATURE_PROCESS_URL))
                .hasAuthority(ADMIN)
                .requestMatchers(antMatcher(DELETE, FEATURE_PROCESS_URL + "/{type}"))
                .hasAuthority(ADMIN)
                .requestMatchers(antMatcher(
                    PUT,
                    "/api/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/process/{processDefinitionId}/properties"
                )).hasAuthority(ADMIN)
            );
        } catch (Exception e) {
            throw new HttpConfigurerConfigurationException(e);
        }
    }

}
