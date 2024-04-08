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

package com.ritense.objectmanagement.security.config

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher

class ObjectManagementHttpSecurityConfigurer : HttpSecurityConfigurer {

    override fun configure(http: HttpSecurity) {
        try {
            http.authorizeHttpRequests { requests ->
                requests.requestMatchers(antMatcher(POST, CONFIGURATION_URL)).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, "$CONFIGURATION_URL/{id}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, CONFIGURATION_URL)).authenticated()
                    .requestMatchers(antMatcher(PUT, CONFIGURATION_URL)).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(DELETE, "$CONFIGURATION_URL/{id}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, "$CONFIGURATION_URL/{id}/object")).authenticated()
                    .requestMatchers(antMatcher(POST, "$CONFIGURATION_URL/{id}/object")).authenticated()

                    .requestMatchers(antMatcher(GET, CONFIGURATION_MANAGEMENT_URL)).hasAuthority(ADMIN)
            }
        } catch (e: Exception) {
            throw HttpConfigurerConfigurationException(e)
        }
    }

    companion object {
        private const val CONFIGURATION_URL = "/api/v1/object/management/configuration"
        private const val CONFIGURATION_MANAGEMENT_URL = "/api/management/v1/object/management/configuration"
    }
}
