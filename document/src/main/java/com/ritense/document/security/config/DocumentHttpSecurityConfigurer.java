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

package com.ritense.document.security.config;

import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException;
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

public class DocumentHttpSecurityConfigurer implements HttpSecurityConfigurer {

    @Override
    public void configure(HttpSecurity http) {
        try {
            http.authorizeRequests()
                .antMatchers(GET, "/api/v1/document/{id}").authenticated()
                .antMatchers(POST, "/api/v1/document").authenticated()
                .antMatchers(PUT, "/api/v1/document").authenticated()
                .antMatchers(POST, "/api/v1/document/{document-id}/resource/{resource-id}").authenticated()
                .antMatchers(DELETE, "/api/v1/document/{document-id}/resource/{resource-id}").authenticated()
                .antMatchers(POST, "/api/v1/document/{documentId}/assign").authenticated()
                .antMatchers(POST, "/api/v1/document/assign").authenticated()
                .antMatchers(POST, "/api/v1/document/{documentId}/unassign").authenticated()
                .antMatchers(GET, "/api/v1/document/{document-id}/candidate-user").authenticated()
                .antMatchers(POST, "/api/v1/document/candidate-user").authenticated();
        } catch (Exception e) {
            throw new HttpConfigurerConfigurationException(e);
        }
    }

}
