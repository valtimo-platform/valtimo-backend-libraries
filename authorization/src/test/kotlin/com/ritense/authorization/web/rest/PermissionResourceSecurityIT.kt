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

import com.ritense.authorization.permission.PermissionRepository
import com.ritense.authorization.role.Role
import com.ritense.authorization.role.RoleRepository
import com.ritense.authorization.permission.ConditionContainer
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.testimpl.TestEntity
import com.ritense.authorization.testimpl.TestEntityActionProvider
import com.ritense.authorization.web.request.PermissionAvailableRequest
import com.ritense.authorization.web.request.PermissionContext
import com.ritense.valtimo.contract.utils.TestUtil
import com.ritense.valtimo.web.rest.SecuritySpecificEndpointIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.util.UUID

class PermissionResourceSecurityIT : SecuritySpecificEndpointIntegrationTest() {

    @Autowired
    lateinit var roleRepository: RoleRepository

    @Autowired
    lateinit var permissionRepository: PermissionRepository

    @Test
    @WithMockUser(authorities = ["test-role"])
    fun `should be able to request permissions when authenticated`() {

        val role = roleRepository.save(Role(key = "test-role"))

        val permissions = listOf(
            Permission(
                UUID.randomUUID(),
                TestEntity::class.java,
                TestEntityActionProvider.view,
                ConditionContainer(emptyList()),
                role
            )
        )

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

        val request = MockMvcRequestBuilders.request(HttpMethod.POST, "/api/v1/permissions")
        request.content(TestUtil.convertObjectToJsonBytes(permissionRequests))
        request.contentType(MediaType.APPLICATION_JSON)
        request.accept(MediaType.APPLICATION_JSON)
        request.with { r: MockHttpServletRequest ->
            r.remoteAddr = "8.8.8.8"
            r
        }
        assertHttpStatus(request, HttpStatus.OK)
    }

    @Test
    fun `should not be able to request permissions when not authenticated`() {

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

        val request = MockMvcRequestBuilders.request(HttpMethod.POST, "/api/v1/permissions")
        request.content(TestUtil.convertObjectToJsonBytes(permissionRequests))
        request.contentType(MediaType.APPLICATION_JSON)
        request.accept(MediaType.APPLICATION_JSON)
        request.with { r: MockHttpServletRequest ->
            r.remoteAddr = "8.8.8.8"
            r
        }
        assertHttpStatus(request, HttpStatus.FORBIDDEN)
    }

}