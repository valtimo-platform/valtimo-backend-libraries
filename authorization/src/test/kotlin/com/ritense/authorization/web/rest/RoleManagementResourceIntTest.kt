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

import com.ritense.authorization.BaseIntegrationTest
import com.ritense.authorization.PermissionRepository
import com.ritense.authorization.RoleRepository
import com.ritense.authorization.web.rest.request.DeleteRolesRequest
import com.ritense.authorization.web.rest.request.SaveRoleRequest
import com.ritense.authorization.web.rest.request.UpdateRolePermissionRequest
import com.ritense.authorization.web.rest.request.UpdateRoleRequest
import com.ritense.valtimo.contract.utils.TestUtil
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class RoleManagementResourceIntTest : BaseIntegrationTest() {
    @Autowired
    lateinit var roleRepository: RoleRepository
    @Autowired
    lateinit var permissionRepository: PermissionRepository

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun init() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()
    }

    @Test
    fun `should retrieve roles`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/management/v1/roles")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.*", Matchers.hasSize<Int>(
                    Matchers.equalTo(roleRepository.findAll().size))
                )
            )

    }

    @Test
    fun `should create role if key does not exist`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/management/v1/roles")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .content(TestUtil.convertObjectToJsonBytes(SaveRoleRequest("TEST_ROLE")))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$").isMap)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.roleKey").value("TEST_ROLE")
            )
    }

    @Test
    fun `should not create role if key already exists`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/management/v1/roles")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .content(TestUtil.convertObjectToJsonBytes(SaveRoleRequest("ROLE_USER")))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isConflict)
    }

    @Test
    fun `should update role if role exists`() {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/management/v1/roles/ROLE_UPDATE")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .content(TestUtil.convertObjectToJsonBytes(UpdateRoleRequest("ROLE_UPDATE2")))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$").isMap)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.roleKey").value("ROLE_UPDATE2")
            )
    }

    @Test
    fun `should not update role if role does not exist`() {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/management/v1/roles/DOES_NOT_EXIST")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .content(TestUtil.convertObjectToJsonBytes(UpdateRoleRequest("ROLE_UPDATE2")))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is5xxServerError)
    }

    @Test
    fun `should delete roles and permissions if roles exist`() {
        val roleCountBefore = roleRepository.findAll().size
        val permissionCountBefore = permissionRepository.findAll().size

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/management/v1/roles")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .content(TestUtil.convertObjectToJsonBytes(DeleteRolesRequest(listOf("TO_REMOVE_ROLE_1", "TO_REMOVE_ROLE_2"))))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

        assertEquals(roleCountBefore - 2, roleRepository.findAll().size)
        assertEquals(permissionCountBefore - 1, permissionRepository.findAll().size)
    }

    @Test
    fun `should not delete roles and permissions if roles do not exist`() {
        val roleCountBefore = roleRepository.findAll().size
        val permissionCountBefore = permissionRepository.findAll().size

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/management/v1/roles")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .content(TestUtil.convertObjectToJsonBytes(DeleteRolesRequest(listOf("DOES_NOT_EXIST"))))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

        assertEquals(roleCountBefore, roleRepository.findAll().size)
        assertEquals(permissionCountBefore, permissionRepository.findAll().size)
    }

    @Test
    fun `should retrieve role permissions if role has permissions`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/management/v1/roles/ROLE_USER/permissions")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.*", Matchers.hasSize<Int>(
                    Matchers.equalTo(permissionRepository.findAllByRoleKeyInOrderByRoleKeyAscResourceTypeAsc(listOf("ROLE_USER")).size))
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$[0].id").doesNotExist()
            )
    }

    @Test
    fun `should not retrieve role permissions if role has no permissions`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/management/v1/roles/ROLE_ADMIN/permissions")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$").isEmpty)
    }

    @Test
    fun `should update role permissions if role exists`() {
        val oldRolePermissions = permissionRepository.findAllByRoleKeyInOrderByRoleKeyAscResourceTypeAsc(listOf("ROLE_USER"))

        mockMvc.perform(MockMvcRequestBuilders.put("/api/management/v1/roles/ROLE_USER/permissions")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .content(
                TestUtil.convertObjectToJsonBytes(
                    listOf(
                        UpdateRolePermissionRequest(
                            oldRolePermissions[0].resourceType,
                            oldRolePermissions[0].action,
                            oldRolePermissions[0].conditionContainer
                        )
                    )
                )
            )
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.*", Matchers.hasSize<Int>(
                    Matchers.equalTo(permissionRepository.findAllByRoleKeyInOrderByRoleKeyAscResourceTypeAsc(listOf("ROLE_USER")).size))
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$[0].id").doesNotExist()
            )

        assertNotEquals(oldRolePermissions[0].id, permissionRepository.findAllByRoleKeyInOrderByRoleKeyAscResourceTypeAsc(listOf("ROLE_USER"))[0].id)
    }

    @Test
    fun `should not update role permissions if role does not exist`() {
        val oldRolePermissions = permissionRepository.findAllByRoleKeyInOrderByRoleKeyAscResourceTypeAsc(listOf("ROLE_USER"))

        mockMvc.perform(MockMvcRequestBuilders.put("/api/management/v1/roles/NOT_EXISTING_ROLE/permissions")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .content(
                TestUtil.convertObjectToJsonBytes(
                    listOf(
                        UpdateRolePermissionRequest(
                            oldRolePermissions[0].resourceType,
                            oldRolePermissions[0].action,
                            oldRolePermissions[0].conditionContainer
                        )
                    )
                )
            )
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is5xxServerError)

        assertEquals(oldRolePermissions, permissionRepository.findAllByRoleKeyInOrderByRoleKeyAscResourceTypeAsc(listOf("ROLE_USER")))
    }
}
