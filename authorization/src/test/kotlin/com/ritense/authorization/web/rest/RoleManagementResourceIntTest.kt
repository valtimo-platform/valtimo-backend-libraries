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
import com.ritense.authorization.RoleRepository
import com.ritense.authorization.web.rest.request.SaveRoleRequest
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

class RoleManagementResourceIntTest : BaseIntegrationTest() {
    @Autowired
    lateinit var roleRepository: RoleRepository

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
                MockMvcResultMatchers.jsonPath("$.key").value("TEST_ROLE")
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
}
