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

package com.ritense.formlink.security.config;

import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException;
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Deprecated(since = "10.6.0", forRemoval = true)
public class ProcessLinkHttpSecurityConfigurer implements HttpSecurityConfigurer {

    public ProcessLinkHttpSecurityConfigurer() {
        //Default constructor
    }

    @Override
    public void configure(HttpSecurity http) {
        try {
            http.authorizeRequests()
                .antMatchers(GET, "/api/v1/process-link/task/{taskId}").hasAuthority(USER)
                .antMatchers(POST, "/api/v1/process-definition/{processDefinitionKey}/form-flow").hasAuthority(USER);
        } catch (Exception e) {
            throw new HttpConfigurerConfigurationException(e);
        }
    }

}
