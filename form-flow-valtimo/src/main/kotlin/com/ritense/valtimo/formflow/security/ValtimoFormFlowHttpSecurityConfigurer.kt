/*
 *  Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.valtimo.formflow.security

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher

class ValtimoFormFlowHttpSecurityConfigurer : HttpSecurityConfigurer {
    override fun configure(http: HttpSecurity) {
        try {
            http.authorizeHttpRequests { requests ->
                requests.requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/form-flow/{formFlowInstanceId}")).authenticated()
                    .requestMatchers(antMatcher(HttpMethod.POST, "/api/v1/form-flow/{formFlowId}/step/{stepInstanceId}")).authenticated()
                    .requestMatchers(antMatcher(HttpMethod.POST, "/api/v1/form-flow/{formFlowId}/back")).authenticated()
                    .requestMatchers(antMatcher(HttpMethod.POST, "/api/v1/form-flow/{formFlowId}/save")).authenticated()
                    .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/process-link/form-flow-definition")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/form-flow/instance/{formFlowInstanceId}")).authenticated()
                    .requestMatchers(antMatcher(HttpMethod.POST, "/api/v1/form-flow/instance/{formFlowId}/step/instance/{stepInstanceId}")).authenticated()
                    .requestMatchers(antMatcher(HttpMethod.POST, "/api/v1/form-flow/instance/{formFlowId}/back")).authenticated()
                    .requestMatchers(antMatcher(HttpMethod.POST, "/api/v1/form-flow/instance/{formFlowId}/save")).authenticated()
                    .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/form-flow/definition")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(HttpMethod.GET, "/api/management/v1/form-flow/definition")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(HttpMethod.GET, "/api/management/v1/form-flow/definition/{key}/{version}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(HttpMethod.DELETE, "/api/management/v1/form-flow/definition/{key}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(HttpMethod.GET, "/api/management/v1/form-flow/definition/{key}/{version}/download")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(HttpMethod.POST, "/api/management/v1/form-flow/definition")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(HttpMethod.PUT, "/api/management/v1/form-flow/definition/{key}")).hasAuthority(ADMIN)
            }
        } catch (e: Exception) {
            throw HttpConfigurerConfigurationException(e)
        }
    }
}
