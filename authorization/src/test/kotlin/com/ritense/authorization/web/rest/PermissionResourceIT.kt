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
import com.ritense.authorization.BaseIntegrationTest
import com.ritense.authorization.permission.PermissionRepository
import com.ritense.authorization.role.Role
import com.ritense.authorization.role.RoleRepository
import com.ritense.authorization.permission.ConditionContainer
import com.ritense.authorization.permission.condition.ContainerPermissionCondition
import com.ritense.authorization.permission.condition.FieldPermissionCondition
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.permission.condition.PermissionConditionOperator
import com.ritense.authorization.testimpl.RelatedTestEntity
import com.ritense.authorization.testimpl.TestEntity
import com.ritense.authorization.testimpl.TestEntityActionProvider
import com.ritense.authorization.web.request.PermissionAvailableRequest
import com.ritense.authorization.web.request.PermissionContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.UUID
import javax.transaction.Transactional

@Transactional
class PermissionResourceIT: BaseIntegrationTest() {

    @Autowired
    lateinit var roleRepository: RoleRepository

    @Autowired
    lateinit var permissionRepository: PermissionRepository

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build()

        roleRepository.deleteByKeyIn(listOf("test-role"))
        roleRepository.save(Role(key = "test-role"))
    }

    @Test
    @WithMockUser(authorities = ["test-role"])
    fun `requesting permission for resource with permission returns available true`() {
        val role = roleRepository.findByKey("test-role")!!
        val permissions = listOf(
            Permission(
                UUID.randomUUID(),
                TestEntity::class.java,
                TestEntityActionProvider.view,
                ConditionContainer(emptyList()),
                role
            )
        )

        permissionRepository.deleteAll()
        permissionRepository.saveAllAndFlush(permissions)

        val permissionRequests = listOf(
            PermissionAvailableRequest(
                "com.ritense.authorization.testimpl.TestEntity",
                "view",
                PermissionContext(
                    "com.ritense.authorization.testimpl.TestEntity",
                    "123"
                )
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(permissionRequests))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect { jsonPath("$[0].resource").value("com.ritense.authorization.testimpl.TestEntity") }
            .andExpect { jsonPath("$[0].action").value("view") }
            .andExpect { jsonPath("$[0].available").value(true) }
    }

    @Test
    @WithMockUser(authorities = ["test-role"])
    fun `requesting permission for resource without permission returns available false`() {
        permissionRepository.deleteAll()

        val permissionRequests = listOf(
            PermissionAvailableRequest(
                "com.ritense.authorization.testimpl.TestEntity",
                "view",
                PermissionContext(
                    "com.ritense.authorization.testimpl.TestEntity",
                    "123"
                )
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(permissionRequests))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect { jsonPath("$[0].resource").value("com.ritense.authorization.testimpl.TestEntity") }
            .andExpect { jsonPath("$[0].action").value("view") }
            .andExpect { jsonPath("$[0].available").value(false) }
    }

    @Test
    @WithMockUser(authorities = ["test-role"])
    fun `requesting permission for resource with permission and conditions returns available true`() {

        val role = roleRepository.findByKey("test-role")!!
        val permissions = listOf(
            Permission(
                UUID.randomUUID(),
                TestEntity::class.java,
                TestEntityActionProvider.view,
                ConditionContainer(listOf(
                    FieldPermissionCondition(
                        "name",
                        PermissionConditionOperator.EQUAL_TO,
                        "test"
                    )
                )),
                role
            )
        )

        permissionRepository.deleteAll()
        permissionRepository.saveAllAndFlush(permissions)

        val permissionRequests = listOf(
            PermissionAvailableRequest(
                "com.ritense.authorization.testimpl.TestEntity",
                "view",
                PermissionContext(
                    "com.ritense.authorization.testimpl.TestEntity",
                    "123"
                )
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(permissionRequests))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect { jsonPath("$[0].resource").value("com.ritense.authorization.testimpl.TestEntity") }
            .andExpect { jsonPath("$[0].action").value("view") }
            .andExpect { jsonPath("$[0].available").value(true) }
    }

    @Test
    @WithMockUser(authorities = ["test-role"])
    fun `requesting permission for resource with permission but not matching conditions returns available false`() {
        val role = roleRepository.findByKey("test-role")!!
        val permissions = listOf(
            Permission(
                UUID.randomUUID(),
                TestEntity::class.java,
                TestEntityActionProvider.view,
                ConditionContainer(listOf(
                    FieldPermissionCondition(
                        "name",
                        PermissionConditionOperator.EQUAL_TO,
                        "other-value"
                    )
                )),
                role
            )
        )

        permissionRepository.deleteAll()
        permissionRepository.saveAllAndFlush(permissions)

        val permissionRequests = listOf(
            PermissionAvailableRequest(
                "com.ritense.authorization.testimpl.TestEntity",
                "view",
                PermissionContext(
                    "com.ritense.authorization.testimpl.TestEntity",
                    "123"
                )
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(permissionRequests))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect { jsonPath("$[0].resource").value("com.ritense.authorization.testimpl.TestEntity") }
            .andExpect { jsonPath("$[0].action").value("view") }
            .andExpect { jsonPath("$[0].available").value(false) }
    }

    @Test
    @WithMockUser(authorities = ["test-role"])
    fun `requesting permission for resource with permission and context of related entity returns available true`() {
        val role = roleRepository.findByKey("test-role")!!
        val permissions = listOf(
            Permission(
                UUID.randomUUID(),
                TestEntity::class.java,
                TestEntityActionProvider.view,
                ConditionContainer(listOf(
                    ContainerPermissionCondition(
                        RelatedTestEntity::class.java,
                        listOf(
                            FieldPermissionCondition(
                                "property",
                                PermissionConditionOperator.EQUAL_TO,
                                "test"
                            )
                        )
                    )
                )),
                role
            )
        )

        permissionRepository.deleteAll()
        permissionRepository.saveAllAndFlush(permissions)

        val permissionRequests = listOf(
            PermissionAvailableRequest(
                "com.ritense.authorization.testimpl.TestEntity",
                "view",
                PermissionContext(
                    "com.ritense.authorization.testimpl.RelatedTestEntity",
                    "123"
                )
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(permissionRequests))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect { jsonPath("$[0].resource").value("com.ritense.authorization.testimpl.TestEntity") }
            .andExpect { jsonPath("$[0].action").value("view") }
            .andExpect { jsonPath("$[0].available").value(true) }
    }

    @Test
    @WithMockUser(authorities = ["test-role"])
    fun `requesting permission for resource with permission and context of related entity with no permission returns available false`() {
        val role = roleRepository.findByKey("test-role")!!
        val permissions = listOf(
            Permission(
                UUID.randomUUID(),
                TestEntity::class.java,
                TestEntityActionProvider.view,
                ConditionContainer(listOf(
                    ContainerPermissionCondition(
                        RelatedTestEntity::class.java,
                        listOf(
                            FieldPermissionCondition(
                                "property",
                                PermissionConditionOperator.EQUAL_TO,
                                "other-value"
                            )
                        )
                    )
                )),
                role
            )
        )

        permissionRepository.deleteAll()
        permissionRepository.saveAllAndFlush(permissions)

        val permissionRequests = listOf(
            PermissionAvailableRequest(
                "com.ritense.authorization.testimpl.TestEntity",
                "view",
                PermissionContext(
                    "com.ritense.authorization.testimpl.RelatedTestEntity",
                    "123"
                )
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(permissionRequests))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect { jsonPath("$[0].resource").value("com.ritense.authorization.testimpl.TestEntity") }
            .andExpect { jsonPath("$[0].action").value("view") }
            .andExpect { jsonPath("$[0].available").value(false) }
    }

    @Test
    fun `requesting permission for not existing resource returns 403 forbidden`() {

        val permissionRequests = listOf(
            PermissionAvailableRequest(
                "test",
                "update",
                PermissionContext(
                    "test",
                    "123"
                )
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(permissionRequests))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `requesting permission for resource with no specification returns 403 forbidden`() {

        val permissionRequests = listOf(
            PermissionAvailableRequest(
                "java.lang.String",
                "view",
                PermissionContext(
                    "com.ritense.authorization.testimpl.TestEntity",
                    "123"
                )
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(permissionRequests))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `requesting permission for resource with context with no specification returns 403 forbidden`() {

        val permissionRequests = listOf(
            PermissionAvailableRequest(
                "com.ritense.authorization.testimpl.TestEntity",
                "view",
                PermissionContext(
                    "java.lang.String",
                    "123"
                )
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(permissionRequests))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }
}