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

package com.ritense.valtimo.security

import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher

class ValtimoCoreSecurityFactory(
    private val httpSecurityConfigurers: List<HttpSecurityConfigurer>
) : CoreSecurityFactory {

    override fun createSecurityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        logger.debug("Configuring httpSecurity")
        httpSecurityConfigurers.forEach { httpConfig: HttpSecurityConfigurer ->
            logger.debug("Configure {}", httpConfig.javaClass)
            httpConfig.configure(httpSecurity)
        }
        return httpSecurity.build()
    }

    override fun createWebSecurityCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web ->
            web.ignoring()
                .requestMatchers(antMatcher(HttpMethod.OPTIONS, "/**"))
                .requestMatchers(antMatcher("/content/**")) }
    }


    private companion object {
        val logger: KLogger = KotlinLogging.logger {}
    }

}