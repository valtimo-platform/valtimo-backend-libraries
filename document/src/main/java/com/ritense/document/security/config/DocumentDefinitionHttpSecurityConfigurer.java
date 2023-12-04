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
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

public class DocumentDefinitionHttpSecurityConfigurer implements HttpSecurityConfigurer {

    @Override
    public void configure(HttpSecurity http) {
        try {
            http.authorizeHttpRequests((requests) -> {
                requests.requestMatchers(GET, "/api/v1/document-definition").authenticated()
                    .requestMatchers(GET, "/api/v1/document-definition").authenticated()
                    .requestMatchers(GET, "/api/v1/document-definition/{name}").authenticated()
                    .requestMatchers(GET, "/api/v1/document-definition/open/count").authenticated()
                    .requestMatchers(GET, "/api/management/v1/document-definition").hasAuthority(ADMIN)
                    .requestMatchers(GET, "/api/management/v1/document-definition/{name}").hasAuthority(ADMIN)
                    .requestMatchers(
                        GET, "/api/management/v1/document-definition/{name}/version/{version}").hasAuthority(ADMIN)
                    .requestMatchers(POST, "/api/v1/document-definition").hasAuthority(ADMIN) // Deprecated since v11
                    .requestMatchers(POST, "/api/management/v1/document-definition").hasAuthority(ADMIN)
                    .requestMatchers(DELETE, "/api/v1/document-definition/{name}").hasAuthority(
                        ADMIN) // Deprecated since v11
                    .requestMatchers(DELETE, "/api/management/v1/document-definition/{name}").hasAuthority(ADMIN);
            });
        } catch (Exception e) {
            throw new HttpConfigurerConfigurationException(e);
        }
    }

}
