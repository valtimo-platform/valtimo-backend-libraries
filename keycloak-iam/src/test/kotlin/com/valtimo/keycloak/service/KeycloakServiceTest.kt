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
package com.valtimo.keycloak.service

import com.ritense.valtimo.contract.security.jwt.JwtConstants
import com.valtimo.keycloak.security.config.ValtimoKeycloakPropertyResolver
import com.valtimo.keycloak.security.jwt.authentication.KeycloakTokenAuthenticator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationContext
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository

@ExtendWith(MockitoExtension::class)
class KeycloakServiceTest(
    @Mock private val keycloakProperties: KeycloakSpringBootProperties,
    @Mock private val applicationContext: ApplicationContext
) {

    private lateinit var keycloakService:KeycloakService

    @BeforeEach
    fun setup() {
        whenever(applicationContext.getBeansOfType(ClientRegistrationRepository::class.java)).thenReturn(mapOf<String, ClientRegistrationRepository>())
        ValtimoKeycloakPropertyResolver(keycloakProperties, applicationContext)

        keycloakService = KeycloakService(keycloakProperties, CLIENT_NAME)
    }

    @Test
    fun `should return empty roles if claims map is empty`() {
        val roles = assertDoesNotThrow {
            keycloakService.getRoles(mapOf<String, Any>())
        }

        assertThat(roles).isEmpty()
    }

    @Test
    fun `should return empty roles if realm ROLES_SCOPE does not exist`() {
        val roles = assertDoesNotThrow {
            keycloakService.getRoles(mapOf<String, Any>(
                KeycloakTokenAuthenticator.REALM_ACCESS to mapOf<String, Any>()
            ))
        }

        assertThat(roles).isEmpty()
    }

    @Test
    fun `should return empty roles if client ROLES_SCOPE does not exist`() {
        val roles = assertDoesNotThrow {
            keycloakService.getRoles(mapOf<String, Any>(
                KeycloakTokenAuthenticator.RESOURCE_ACCESS to mapOf<String, Any>(
                    CLIENT_NAME to mapOf<String, Any>()
                )
            ))
        }

        assertThat(roles).isEmpty()
    }

    @Test
    fun `should return realm roles only`() {
        val realmRole = "ROLE_REALM"
        val roles = assertDoesNotThrow {
            val claims = mapOf<String, Any>(
                KeycloakTokenAuthenticator.REALM_ACCESS to mapOf(
                    JwtConstants.ROLES_SCOPE to listOf(realmRole)
                ),
                KeycloakTokenAuthenticator.RESOURCE_ACCESS to mapOf(
                    "invalid_client" to mapOf(
                        JwtConstants.ROLES_SCOPE to listOf("ROLE_INVALID")
                    )
                )
            )
            keycloakService.getRoles(claims)
        }

        assertThat(roles).containsExactlyInAnyOrder(realmRole)
    }

    @Test
    fun `should return client roles and realm roles`() {
        val realmRole = "ROLE_REALM"
        val clientRole = "CLIENT_ROLE"
        val roles = assertDoesNotThrow {
            val claims = mapOf<String, Any>(
                KeycloakTokenAuthenticator.REALM_ACCESS to mapOf(
                    JwtConstants.ROLES_SCOPE to listOf(realmRole)
                ),
                KeycloakTokenAuthenticator.RESOURCE_ACCESS to mapOf(
                    "invalid_client" to mapOf(
                        JwtConstants.ROLES_SCOPE to listOf("ROLE_INVALID")
                    ),
                    CLIENT_NAME to mapOf(
                        JwtConstants.ROLES_SCOPE to listOf(clientRole)
                    )
                )
            )
            keycloakService.getRoles(claims)
        }

        assertThat(roles).containsExactlyInAnyOrder(realmRole, clientRole)
    }

    @Test
    fun `should return client roles only`() {
        val clientRole = "CLIENT_ROLE"
        val roles = assertDoesNotThrow {
            val claims = mapOf<String, Any>(
                KeycloakTokenAuthenticator.RESOURCE_ACCESS to mapOf(
                    "invalid_client" to mapOf(
                        JwtConstants.ROLES_SCOPE to listOf("ROLE_INVALID")
                    ),
                    CLIENT_NAME to mapOf(
                        JwtConstants.ROLES_SCOPE to listOf(clientRole)
                    )
                )
            )
            keycloakService.getRoles(claims)
        }

        assertThat(roles).containsExactlyInAnyOrder(clientRole)
    }

    companion object {
        const val CLIENT_NAME = "test-client"
    }
}

