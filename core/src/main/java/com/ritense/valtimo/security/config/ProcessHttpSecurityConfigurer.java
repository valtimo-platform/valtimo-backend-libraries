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

import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException;
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

public class ProcessHttpSecurityConfigurer implements HttpSecurityConfigurer {

    @Override
    public void configure(HttpSecurity http) {
        try {
            http.authorizeRequests()
                    .antMatchers(GET, "/api/v1/process/definition").authenticated()
                    .antMatchers(POST, "/api/v1/process/definition/{processDefinitionId}/count").authenticated()
                    .antMatchers(GET, "/api/v1/process/definition/{processDefinitionId}/xml").authenticated()
                    .antMatchers(PUT, "/api/v1/process/definition/{processDefinitionId}/xml/timer").authenticated()
                    .antMatchers(GET, "/api/v1/process/definition/{processDefinitionKey}/search-properties").authenticated()
                    .antMatchers(GET, "/api/v1/process/definition/{processDefinitionKey}").authenticated()
                    .antMatchers(GET, "/api/v1/process/definition/{processDefinitionKey}/versions").authenticated()
                    .antMatchers(GET, "/api/v1/process/definition/{processDefinitionKey}/start-form").authenticated()
                    .antMatchers(GET, "/api/v1/process/definition/{processDefinitionKey}/usertasks").authenticated()
                    .antMatchers(GET, "/api/v1/process/definition/{processDefinitionKey}/heatmap/count").authenticated()
                    .antMatchers(GET, "/api/v1/process/definition/{processDefinitionKey}/heatmap/duration").authenticated()
                    .antMatchers(POST, "/api/v1/process/definition/{processDefinitionKey}/{businessKey}/start").authenticated()
                    .antMatchers(GET, "/api/v1/process/definition/{sourceProcessDefinitionId}/{targetProcessDefinitionId}/flownodes").authenticated()
                    .antMatchers(POST, "/api/v1/process/definition/{sourceProcessDefinitionId}/{targetProcessDefinitionId}/migrate").hasAuthority(ADMIN)
                    .antMatchers(GET, "/api/v1/process/{processInstanceId}").authenticated()
                    .antMatchers(GET, "/api/v1/process/{processInstanceId}/history").authenticated()
                    .antMatchers(GET, "/api/v1/process/{processInstanceId}/log").authenticated()
                    .antMatchers(GET, "/api/v1/process/{processInstanceId}/tasks").authenticated()
                    .antMatchers(GET, "/api/v1/process/{processInstanceId}/activetask").authenticated()
                    .antMatchers(GET, "/api/v1/process/{processInstanceId}/xml").authenticated()
                    .antMatchers(GET, "/api/v1/process/{processInstanceId}/activities").authenticated()
                    .antMatchers(GET, "/api/v1/process/{processInstanceId}/comments").authenticated()
                    .antMatchers(GET, "/api/v1/process/{processDefinitionName}/search").authenticated()
                    .antMatchers(POST, "/api/v1/process/{processDefinitionName}/count").authenticated()
                    .antMatchers(POST, "/api/v1/process/{processInstanceId}/comment").authenticated()
                    .antMatchers(POST, "/api/v1/process/{processInstanceId}/delete").hasAuthority(ADMIN)
                    .antMatchers(POST, "/api/v1/process/definition/deployment").hasAuthority(ADMIN);
        } catch (Exception e) {
            throw new HttpConfigurerConfigurationException(e);
        }
    }

}
