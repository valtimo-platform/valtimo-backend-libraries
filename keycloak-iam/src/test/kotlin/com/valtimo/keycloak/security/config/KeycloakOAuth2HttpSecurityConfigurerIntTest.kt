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

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER
import com.ritense.valtimo.contract.security.jwt.JwtConstants.EMAIL_KEY
import com.ritense.valtimo.contract.security.jwt.JwtConstants.ROLES_SCOPE
import com.ritense.valtimo.contract.utils.SecurityUtils
import com.valtimo.keycloak.BaseIntegrationTest
import com.valtimo.keycloak.security.jwt.authentication.KeycloakTokenAuthenticator.REALM_ACCESS
import com.valtimo.keycloak.security.jwt.authentication.KeycloakTokenAuthenticator.RESOURCE_ACCESS
import org.junit.jupiter.api.Test
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class KeycloakOAuth2HttpSecurityConfigurerIntTest : BaseIntegrationTest() {

    @Test
    @WithMockKeycloakUser(
        email = "user@ritense.com",
        roles = [USER],
        clientId = "valtimo-console",
        clientRoles = [ADMIN]
    )
    fun `should retrieve email and roles from keycloak JWT token`() {
        val authentication = SecurityContextHolder.getContext().authentication
        assertTrue(authentication is JwtAuthenticationToken)
        assertEquals("user@ritense.com", authentication.name)
        assertEquals("user@ritense.com", authentication.token.getClaimAsString(EMAIL_KEY))
        assertEquals("user@ritense.com", SecurityUtils.getCurrentUserLogin())
        assertContains(authentication.authorities.map { it.authority }, USER)
        assertContains(authentication.token.getClaimAsMap(REALM_ACCESS)[ROLES_SCOPE] as List<String>, USER)
        assertContains(SecurityUtils.getCurrentUserRoles(), USER)
        assertContains(authentication.authorities.map { it.authority }, ADMIN)
        assertContains(
            (authentication.token.getClaimAsMap(RESOURCE_ACCESS)["valtimo-console"] as Map<String, Any>)[ROLES_SCOPE] as List<String>,
            ADMIN
        )
        assertContains(SecurityUtils.getCurrentUserRoles(), ADMIN)
    }

}