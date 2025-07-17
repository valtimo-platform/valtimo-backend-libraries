/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.iko.security.config

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher

class IkoHttpSecurityConfigurer : HttpSecurityConfigurer {

    override fun configure(http: HttpSecurity) {
        try {
            http.authorizeHttpRequests { requests ->
                requests
                    .requestMatchers(antMatcher(GET, "/api/management/v1/iko-types")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, "/api/management/v1/iko-property-fields/{type}/repository-config")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, "/api/management/v1/iko")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, "/api/management/v1/iko/{key}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(POST, "/api/management/v1/iko/{key}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(PUT, "/api/management/v1/iko/{key}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(DELETE, "/api/management/v1/iko/{key}")).hasAuthority(ADMIN)

                    .requestMatchers(antMatcher(GET, "/api/v1/iko-data-aggregate")).authenticated()
                    .requestMatchers(antMatcher(GET, "/api/management/v1/iko-property-fields/{type}/data-aggregate")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, "/api/management/v1/iko-data-aggregate")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, "/api/management/v1/iko-data-aggregate/{key}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(POST, "/api/management/v1/iko-data-aggregate/{key}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(PUT, "/api/management/v1/iko-data-aggregate/{key}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(DELETE, "/api/management/v1/iko-data-aggregate/{key}")).hasAuthority(ADMIN)

                    .requestMatchers(antMatcher(GET, "/api/v1/iko-data-aggregate/{key}/data-request")).authenticated()
                    .requestMatchers(antMatcher(POST, "/api/v1/iko-data-aggregate/{key}/data-request/{key}/search")).authenticated()
                    .requestMatchers(antMatcher(GET, "/api/management/v1/iko-property-fields/{type}/data-request")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, "/api/management/v1/iko-data-aggregate/{key}/data-request")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, "/api/management/v1/iko-data-aggregate/{key}/data-request/{key}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(POST, "/api/management/v1/iko-data-aggregate/{key}/data-request/{key}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(PUT, "/api/management/v1/iko-data-aggregate/{key}/data-request")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(DELETE, "/api/management/v1/iko-data-aggregate/{key}/data-request/{key}")).hasAuthority(ADMIN)

                    .requestMatchers(antMatcher(GET, "/api/v1/iko-data-aggregate/{key}/tab")).authenticated()
                    .requestMatchers(antMatcher(GET, "/api/management/v1/iko-data-aggregate/{key}/tab")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, "/api/management/v1/iko-data-aggregate/{key}/tab/{key}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(POST, "/api/management/v1/iko-data-aggregate/{key}/tab/{key}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(PUT, "/api/management/v1/iko-data-aggregate/{key}/tab")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(DELETE, "/api/management/v1/iko-data-aggregate/{key}/tab/{key}")).hasAuthority(ADMIN)

                    .requestMatchers(antMatcher(GET,"/api/v1/iko-data-aggregate/{key}/tab/{key}/widget")).authenticated()
                    .requestMatchers(antMatcher(GET,"/api/v1/iko-data-aggregate/{key}/tab/{key}/widget/{key}/data")).authenticated()
                    .requestMatchers(antMatcher(GET,"/api/management/v1/iko-data-aggregate/{key}/tab/{key}/widget")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET,"/api/management/v1/iko-data-aggregate/{key}/tab/{key}/widget/{key}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(POST,"/api/management/v1/iko-data-aggregate/{key}/tab/{key}/widget/{key}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(PUT,"/api/management/v1/iko-data-aggregate/{key}/tab/{key}/widget")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(DELETE,"/api/management/v1/iko-data-aggregate/{key}/tab/{key}/widget/{key}")).hasAuthority(ADMIN)

                    .requestMatchers(antMatcher(GET,"/api/management/v1/iko-data-aggregate/{ikoDataAggregateKey}/column")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET,"/api/management/v1/iko-data-aggregate/{ikoDataAggregateKey}/column/{key}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(POST,"/api/management/v1/iko-data-aggregate/{ikoDataAggregateKey}/column/{key}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(PUT,"/api/management/v1/iko-data-aggregate/{ikoDataAggregateKey}/column")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(DELETE,"/api/management/v1/iko-data-aggregate/{ikoDataAggregateKey}/column/{key}")).hasAuthority(ADMIN)
            }
        } catch (e: Exception) {
            throw HttpConfigurerConfigurationException(e)
        }
    }
}
