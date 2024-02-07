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

package com.ritense.valtimo.security.config;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException;
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public class ChoiceFieldHttpSecurityConfigurer implements HttpSecurityConfigurer {

    @Override
    public void configure(HttpSecurity http) {
        try {
            http.authorizeHttpRequests(requests ->
                requests.requestMatchers(
                    antMatcher(GET, "/api/v1/choice-fields"),
                    antMatcher(GET, "/api/v1/choice-fields/{id}"),
                    antMatcher(GET, "/api/v1/choice-fields/name/{name}"),
                    antMatcher(GET, "/api/v2/choice-fields")
                ).authenticated()
                .requestMatchers(antMatcher(POST, "/api/v1/choice-fields")).hasAuthority(ADMIN)
                .requestMatchers(antMatcher(PUT, "/api/v1/choice-fields")).hasAuthority(ADMIN)
                .requestMatchers(antMatcher(DELETE, "/api/v1/choice-fields/{id}")).hasAuthority(ADMIN)
                //choice-field-values
                .requestMatchers(antMatcher(GET, "/api/v1/choice-field-values")).authenticated()
                .requestMatchers(antMatcher(GET, "/api/v2/choice-field-values")).authenticated()
                .requestMatchers(antMatcher(POST, "/api/v1/choice-field-values")).hasAuthority(ADMIN)
                .requestMatchers(antMatcher(PUT, "/api/v1/choice-field-values")).hasAuthority(ADMIN)
                .requestMatchers(antMatcher(GET, "/api/v1/choice-field-values/{id}")).authenticated()
                .requestMatchers(antMatcher(DELETE, "/api/v1/choice-field-values/{id}")).hasAuthority(ADMIN)
                .requestMatchers(antMatcher(GET, "/api/v1/choice-field-values/choice-field/{choicefield_name}/value/{value}")).authenticated()
                .requestMatchers(antMatcher(GET, "/api/v1/choice-field-values/{choice_field_name}/values")).authenticated()
                .requestMatchers(antMatcher(GET, "/api/v2/choice-field-values/{choice_field_name}/values")).authenticated()
            );
        } catch (Exception e) {
            throw new HttpConfigurerConfigurationException(e);
        }
    }

}