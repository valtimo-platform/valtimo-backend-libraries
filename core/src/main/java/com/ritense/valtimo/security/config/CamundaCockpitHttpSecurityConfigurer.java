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

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

public class CamundaCockpitHttpSecurityConfigurer implements HttpSecurityConfigurer {

    @Override
    public void configure(HttpSecurity http) {
        try {
            //By default cockpit uses BasicAuth using different tech,
            //we just set it open and let Camunda do the rest.
            http.authorizeHttpRequests((requests) ->
                requests.requestMatchers(
                    antMatcher("/app/**")
                ).denyAll() //TODO: 92240 .access("@whitelistIpRequest.check(request)")
                .requestMatchers(
                    antMatcher("/api/admin/**"),
                    antMatcher("/api/cockpit/**"),
                    antMatcher("/api/tasklist/**"),
                    antMatcher("/api/engine/**"),
                    antMatcher("/lib/**")
                ).denyAll() //TODO: 92240 .access("@whitelistIpRequest.check(request)")
            );
        } catch (Exception e) {
            throw new HttpConfigurerConfigurationException(e);
        }
    }

}