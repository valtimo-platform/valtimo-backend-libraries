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

package com.ritense.authorization.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.authorization.PermissionRepository
import com.ritense.authorization.web.rest.request.DeleteRolesRequest
import com.ritense.authorization.web.rest.request.SaveRoleRequest
import com.ritense.authorization.web.rest.request.UpdateRolePermissionRequest
import com.ritense.authorization.web.rest.request.UpdateRoleRequest
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import com.ritense.valtimo.contract.utils.TestUtil
import com.ritense.valtimo.web.rest.SecuritySpecificEndpointIntegrationTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

class RoleManagementResourceSecurityIntTest : SecuritySpecificEndpointIntegrationTest() {
    @Autowired
    lateinit var permissionRepository: PermissionRepository

    @BeforeEach
    fun setUp() {
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should have access to retrieve roles method with role_admin`() {
        assertHttpStatus(GET, "/api/management/v1/roles", HttpStatus.OK)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should not access to retrieve roles method without role_admin`() {
        assertHttpStatus(GET, "/api/management/v1/roles", HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should have access to save role method with role_admin`() {
        val request = MockMvcRequestBuilders.request(POST, "/api/management/v1/roles")
        request.content(TestUtil.convertObjectToJsonBytes(SaveRoleRequest("NONEXISTANT")))
        request.contentType(MediaType.APPLICATION_JSON)
        request.accept(MediaType.APPLICATION_JSON)
        request.with { r: MockHttpServletRequest ->
            r.remoteAddr = "8.8.8.8"
            r
        }
        assertHttpStatus(request, HttpStatus.OK)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should not access to save role method without role_admin`() {
        val request = MockMvcRequestBuilders.request(POST, "/api/management/v1/roles")
        request.content(TestUtil.convertObjectToJsonBytes(SaveRoleRequest("ROLE_ADMIN")))
        request.contentType(MediaType.APPLICATION_JSON)
        request.accept(MediaType.APPLICATION_JSON)
        request.with { r: MockHttpServletRequest ->
            r.remoteAddr = "8.8.8.8"
            r
        }
        assertHttpStatus(request, HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should have access to update role method with role_admin`() {
        val request = MockMvcRequestBuilders.request(PUT, "/api/management/v1/roles/ROLE_ADMIN")
        request.content(TestUtil.convertObjectToJsonBytes(UpdateRoleRequest("ROLE_ADMIN")))
        request.contentType(MediaType.APPLICATION_JSON)
        request.accept(MediaType.APPLICATION_JSON)
        request.with { r: MockHttpServletRequest ->
            r.remoteAddr = "8.8.8.8"
            r
        }
        assertHttpStatus(request, HttpStatus.OK)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should not access to update role method without role_admin`() {
        val request = MockMvcRequestBuilders.request(PUT, "/api/management/v1/roles/ROLE_ADMIN")
        request.content(TestUtil.convertObjectToJsonBytes(UpdateRoleRequest("ROLE_ADMIN")))
        request.contentType(MediaType.APPLICATION_JSON)
        request.accept(MediaType.APPLICATION_JSON)
        request.with { r: MockHttpServletRequest ->
            r.remoteAddr = "8.8.8.8"
            r
        }
        assertHttpStatus(request, HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should have access to delete roles method with role_admin`() {
        val request = MockMvcRequestBuilders.request(DELETE, "/api/management/v1/roles")
        request.content(TestUtil.convertObjectToJsonBytes(DeleteRolesRequest(listOf("NONEXISTANT"))))
        request.contentType(MediaType.APPLICATION_JSON)
        request.accept(MediaType.APPLICATION_JSON)
        request.with { r: MockHttpServletRequest ->
            r.remoteAddr = "8.8.8.8"
            r
        }
        assertHttpStatus(request, HttpStatus.OK)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should not have access to delete roles method without role_admin`() {
        val request = MockMvcRequestBuilders.request(DELETE, "/api/management/v1/roles")
        request.content(TestUtil.convertObjectToJsonBytes(DeleteRolesRequest(listOf("NONEXISTANT"))))
        request.contentType(MediaType.APPLICATION_JSON)
        request.accept(MediaType.APPLICATION_JSON)
        request.with { r: MockHttpServletRequest ->
            r.remoteAddr = "8.8.8.8"
            r
        }
        assertHttpStatus(request, HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should have access to retrieve role permissions method with role_admin`() {
        val request = MockMvcRequestBuilders.request(GET, "/api/management/v1/roles/ROLE_USE/permissions")
        request.contentType(MediaType.APPLICATION_JSON)
        request.accept(MediaType.APPLICATION_JSON)
        request.with { r: MockHttpServletRequest ->
            r.remoteAddr = "8.8.8.8"
            r
        }
        assertHttpStatus(request, HttpStatus.OK)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should not have access to retrieve role permissions method without role_admin`() {
        val request = MockMvcRequestBuilders.request(GET, "/api/management/v1/roles/ROLE_USER/permissions")
        request.contentType(MediaType.APPLICATION_JSON)
        request.accept(MediaType.APPLICATION_JSON)
        request.with { r: MockHttpServletRequest ->
            r.remoteAddr = "8.8.8.8"
            r
        }
        assertHttpStatus(request, HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should have access to update role permissions method with role_admin`() {
        System.out.println(jacksonObjectMapper().writeValueAsString(permissionRepository.findAll()))
        val basePermission = permissionRepository.findAllByRoleKeyInOrderByRoleKeyAscResourceTypeAsc(listOf("test-role"))[0]

        val request = MockMvcRequestBuilders.request(PUT, "/api/management/v1/roles/ROLE_USER/permissions")
        request.content(
            TestUtil.convertObjectToJsonBytes(
                listOf(
                        UpdateRolePermissionRequest(
                        basePermission.resourceType,
                        basePermission.action,
                        basePermission.conditionContainer
                    )
                )
            )
        )
        request.contentType(MediaType.APPLICATION_JSON)
        request.accept(MediaType.APPLICATION_JSON)
        request.with { r: MockHttpServletRequest ->
            r.remoteAddr = "8.8.8.8"
            r
        }
        assertHttpStatus(request, HttpStatus.OK)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should not have access to update role permissions method without role_admin`() {
        val basePermission = permissionRepository.findAllByRoleKeyInOrderByRoleKeyAscResourceTypeAsc(listOf("test-role"))[0]

        val request = MockMvcRequestBuilders.request(PUT, "/api/management/v1/roles/ROLE_USER/permissions")
        request.content(
            TestUtil.convertObjectToJsonBytes(
                listOf(
                    UpdateRolePermissionRequest(
                        basePermission.resourceType,
                        basePermission.action,
                        basePermission.conditionContainer
                    )
                )
            )
        )
        request.contentType(MediaType.APPLICATION_JSON)
        request.accept(MediaType.APPLICATION_JSON)
        request.with { r: MockHttpServletRequest ->
            r.remoteAddr = "8.8.8.8"
            r
        }
        assertHttpStatus(request, HttpStatus.FORBIDDEN)
    }
}