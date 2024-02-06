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

package com.valtimo.keycloak

import com.valtimo.keycloak.service.KeycloakRoleService
import com.valtimo.keycloak.service.KeycloakService
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.RoleRepresentation
import org.mockito.Answers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class KeycloakExternalRoleServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    lateinit var keycloakService: KeycloakService

    @InjectMocks
    lateinit var keycloakRoleService: KeycloakRoleService

    @Test
    fun `should return all keycloak roles found when prefix is null`() {

        // given
        defaultConditions()

        // when
        val result = keycloakRoleService.findExternalRoles(null)

        // then
        assertEquals(TEST_ROLE_NO_PREFIX_LIST, result)
    }

    @Test
    fun `should return roles with TEAM in the name`() {

        // given
        defaultConditions()

        // when
        val result = keycloakRoleService.findExternalRoles("ROLE_TEAM_")

        // then
        assertEquals(TEST_ROLE_LIST, result)
    }

    private fun defaultConditions() {
        whenever(keycloakService.keycloak()).thenReturn(mock<Keycloak>())
        whenever(keycloakService.realmRolesResource(any()).list(any())).thenReturn(roleRepresentationList())
    }

    private fun roleRepresentationList(): List<RoleRepresentation> = listOf(
        RoleRepresentation("ROLE_TEAM_TEST", "", false),
        RoleRepresentation("ROLE_TEAM_ALSO_A_TEST", "", false),
        RoleRepresentation("ROLE_NOPE", "", false),
    )
    companion object {
        private val TEST_ROLE_NO_PREFIX_LIST = listOf("ROLE_TEAM_TEST", "ROLE_TEAM_ALSO_A_TEST", "ROLE_NOPE")
        private val TEST_ROLE_LIST = listOf("ROLE_TEAM_TEST", "ROLE_TEAM_ALSO_A_TEST")
    }
}