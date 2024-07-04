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
package com.ritense.valtimo.contract.security.config

import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

abstract class AuthorizeRequestsHttpSecurityConfigurer : HttpSecurityConfigurer {
    final override fun configure(http: HttpSecurity) {
        try {
            http.authorizeHttpRequests { requests -> this.authorizeHttpRequests(requests) }
        } catch (e: Exception) {
            throw HttpConfigurerConfigurationException(e)
        }
    }

    abstract fun authorizeHttpRequests(requests: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry)

    fun AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry.antMatcher(
        method: HttpMethod,
        pattern: String
    ): AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl = this.requestMatchers(AntPathRequestMatcher.antMatcher(method, pattern))
}