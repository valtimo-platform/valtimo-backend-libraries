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

import com.ritense.valtimo.contract.security.jwt.JwtConstants.EMAIL_KEY
import com.ritense.valtimo.contract.security.jwt.JwtConstants.ROLES_SCOPE
import com.valtimo.keycloak.security.jwt.authentication.KeycloakTokenAuthenticator.REALM_ACCESS
import com.valtimo.keycloak.security.jwt.authentication.KeycloakTokenAuthenticator.RESOURCE_ACCESS
import com.valtimo.keycloak.service.KeycloakService.KEYCLOAK_JWT_CLIENT_REGISTRATION
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.test.context.support.WithSecurityContextFactory
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils

@Component
internal class WithMockKeycloakUserSecurityContextFactory(
    private val clientRegistrationRepository: ClientRegistrationRepository
) : WithSecurityContextFactory<WithMockKeycloakUser> {

    override fun createSecurityContext(withUser: WithMockKeycloakUser): SecurityContext {
        val email: String = if (StringUtils.hasLength(withUser.email)) withUser.email else withUser.value
        check(email.isNotBlank()) {
            "$withUser cannot have null email on both username and value properties"
        }

        val clientRegistration = clientRegistrationRepository.findByRegistrationId(KEYCLOAK_JWT_CLIENT_REGISTRATION)
        val jwtBuilder = Jwt.withTokenValue("test-token")
            .header("typ", "JWT")
            .issuer(clientRegistration.providerDetails.issuerUri)
            .claim(EMAIL_KEY, email)
            .claim(REALM_ACCESS, mapOf(ROLES_SCOPE to withUser.roles.toList()))
        if (withUser.clientId.isNotEmpty()) {
            jwtBuilder.claim(
                RESOURCE_ACCESS,
                mapOf(withUser.clientId to mapOf(ROLES_SCOPE to withUser.clientRoles.toList()))
            )
        }
        val authorities = (withUser.roles + withUser.clientRoles).map { SimpleGrantedAuthority(it) }
        val authentication = JwtAuthenticationToken(jwtBuilder.build(), authorities, email)
        val context = SecurityContextHolder.getContextHolderStrategy().createEmptyContext()
        context.authentication = authentication
        return context
    }
}