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

package com.valtimo.keycloak.security.config

import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer
import com.valtimo.keycloak.service.KeycloakService
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter

class KeycloakOAuth2HttpSecurityConfigurer(
    private val keycloakService: KeycloakService
) : HttpSecurityConfigurer, Converter<Jwt, AbstractAuthenticationToken> {

    override fun configure(http: HttpSecurity) {
        http
            .oauth2ResourceServer { oauth2 -> oauth2.jwt { it.jwtAuthenticationConverter(this@KeycloakOAuth2HttpSecurityConfigurer) } }
            .oauth2Login(withDefaults())
    }

    override fun convert(source: Jwt): AbstractAuthenticationToken {
        val defaultAuthorities = JwtGrantedAuthoritiesConverter().convert(source) ?: emptyList()
        val roleAuthorities = keycloakService.getRoles(source.claims).map { SimpleGrantedAuthority(it) }
        val email = keycloakService.getEmail(source.claims)
        val authorities = defaultAuthorities + roleAuthorities
        return JwtAuthenticationToken(source, authorities, email)
    }
}