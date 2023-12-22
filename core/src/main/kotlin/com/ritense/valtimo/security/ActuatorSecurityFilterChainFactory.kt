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

package com.ritense.valtimo.security

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ACTUATOR
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher
import org.springframework.security.web.util.matcher.OrRequestMatcher

class ActuatorSecurityFilterChainFactory {

    fun createFilterChain(
        http: HttpSecurity,
        webEndpointProperties: WebEndpointProperties,
        passwordEncoder: PasswordEncoder,
        username: String,
        password: String
    ): SecurityFilterChain {
        val matchers = getActuatorMatchers(webEndpointProperties.basePath)
        http
            .securityMatcher(OrRequestMatcher(*matchers))
            .authorizeHttpRequests { it.requestMatchers(*matchers).hasAuthority(ACTUATOR) }
            .authenticationManager(actuatorAuthenticationManager(passwordEncoder, username, password))
            .httpBasic { it.realmName(ACTUATOR_REALM) }

        return http.build()
    }

    private fun actuatorAuthenticationManager(
        passwordEncoder: PasswordEncoder,
        username: String,
        password: String
    ): AuthenticationManager {
        val userDetailsService: UserDetailsService = userDetailsService(passwordEncoder, username, password)
        val authenticationProvider = DaoAuthenticationProvider()
        authenticationProvider.setPasswordEncoder(passwordEncoder)
        authenticationProvider.setUserDetailsService(userDetailsService)

        return ProviderManager(authenticationProvider)
    }

    private fun userDetailsService(
        passwordEncoder: PasswordEncoder,
        username: String,
        password: String
    ): UserDetailsService {
        val actuatorUser: UserDetails = User
            .withUsername(username)
            .password(passwordEncoder.encode(password))
            .authorities(ACTUATOR)
            .build()

        return InMemoryUserDetailsManager(actuatorUser)
    }

    private fun getActuatorMatchers(actuatorPath: String) = arrayOf(
        antMatcher(GET, actuatorPath),
        antMatcher(GET, "${actuatorPath}/configprops"),
        antMatcher(GET, "${actuatorPath}/env"),
        antMatcher(GET, "${actuatorPath}/health"),
        antMatcher(GET, "${actuatorPath}/mappings"),
        antMatcher(GET, "${actuatorPath}/logfile"),
        antMatcher(GET, "${actuatorPath}/loggers"),
        antMatcher(POST, "${actuatorPath}/loggers/**"),
        antMatcher(GET, "${actuatorPath}/info"),
    )

    companion object {
        const val ACTUATOR_REALM = "Actuator realm"
    }
}