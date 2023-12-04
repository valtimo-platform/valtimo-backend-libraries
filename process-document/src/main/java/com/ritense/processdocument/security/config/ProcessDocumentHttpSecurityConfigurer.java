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

package com.ritense.processdocument.security.config;

import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException;
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

public class ProcessDocumentHttpSecurityConfigurer implements HttpSecurityConfigurer {

    @Override
    public void configure(HttpSecurity http) {
        try {
            http.authorizeHttpRequests((requests) -> {
                requests.requestMatchers(GET, "/api/v1/process-document/definition").authenticated()
                    .requestMatchers(POST, "/api/v1/process-document/definition").hasAuthority(ADMIN)
                    .requestMatchers(DELETE, "/api/v1/process-document/definition").hasAuthority(ADMIN)
                    .requestMatchers(
                        GET, "/api/v1/process-document/definition/document/{document-definition-name}").authenticated()
                    .requestMatchers(
                        GET, "/api/v1/process-document/definition/process/{process-definition-key}").hasAuthority(ADMIN)
                    .requestMatchers(
                        GET,
                        "/api/v1/process-document/definition/processinstance/{process-instance-id}"
                    ).authenticated()
                    .requestMatchers(GET, "/api/v1/process-document/instance/document/{document-id}").authenticated()
                    .requestMatchers(
                        GET, "/api/v1/process-document/instance/document/{document-id}/audit").authenticated()
                    .requestMatchers(
                        POST, "/api/v1/process-document/operation/new-document-and-start-process").authenticated()
                    .requestMatchers(
                        POST, "/api/v1/process-document/operation/modify-document-and-complete-task").authenticated()
                    .requestMatchers(
                        POST, "/api/v1/process-document/operation/modify-document-and-start-process").authenticated()
                    .requestMatchers(
                        GET, "/api/v1/process-document/demo/{documentDefinitionName}/process").hasAuthority(ADMIN)
                    .requestMatchers(
                        PUT, "/api/v1/process-document/demo/{documentDefinitionName}/process").hasAuthority(ADMIN)
                    .requestMatchers(
                        DELETE, "/api/v1/process-document/demo/{documentDefinitionName}/process").hasAuthority(ADMIN);
            });
        } catch (Exception e) {
            throw new HttpConfigurerConfigurationException(e);
        }
    }

}
