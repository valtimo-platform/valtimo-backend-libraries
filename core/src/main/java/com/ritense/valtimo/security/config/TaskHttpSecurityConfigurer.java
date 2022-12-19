/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

public class TaskHttpSecurityConfigurer implements HttpSecurityConfigurer {

    private static final String TASK_ACCESS_PERMISSION = "hasAuthority('ROLE_USER') and hasPermission(#taskId, 'taskAccess')";

    @Override
    public void configure(HttpSecurity http) {
        try {
            http.authorizeRequests()
                .antMatchers(GET, "/api/v1/task").hasAuthority(USER)
                .antMatchers(POST, "/api/v1/task/assign/batch-assign").hasAuthority(USER)
                .antMatchers(POST, "/api/v1/task/batch-complete").hasAuthority(USER)
                .antMatchers(GET, "/api/v1/task/{taskId}").access(TASK_ACCESS_PERMISSION)
                .antMatchers(POST, "/api/v1/task/{taskId}/assign").access(TASK_ACCESS_PERMISSION)
                .antMatchers(GET, "/api/v1/task/{taskId}/comments").access(TASK_ACCESS_PERMISSION)
                .antMatchers(POST, "/api/v1/task/{taskId}/complete").access(TASK_ACCESS_PERMISSION)
                .antMatchers(POST, "/api/v1/task/{taskId}/unassign").access(TASK_ACCESS_PERMISSION)
                .antMatchers(GET, "/api/v1/task/{taskId}/candidate-user").access(TASK_ACCESS_PERMISSION);
        } catch (Exception e) {
            throw new HttpConfigurerConfigurationException(e);
        }
    }

}