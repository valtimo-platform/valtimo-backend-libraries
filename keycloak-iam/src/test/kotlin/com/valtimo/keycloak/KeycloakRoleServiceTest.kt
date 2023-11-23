package com.valtimo.keycloak

import com.valtimo.keycloak.service.KeycloakRoleService
import com.valtimo.keycloak.service.KeycloakService
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
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
internal class KeycloakRoleServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    lateinit var keycloakService: KeycloakService

    @InjectMocks
    lateinit var keycloakRoleService: KeycloakRoleService

    @Test
    fun `should return all roles found when prefix is null`() {

        // given
        defaultConditions()

        // when
        val result = keycloakRoleService.findRoles(null)

        // then
        assertEquals(TEST_ROLE_NO_PREFIX_LIST, result)
    }

    @Test
    fun `should return roles with TEAM in the name`() {

        // given
        defaultConditions()

        // when
        val result = keycloakRoleService.findRoles("ROLE_TEAM_")

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