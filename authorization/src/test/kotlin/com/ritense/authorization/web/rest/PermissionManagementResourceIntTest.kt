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
import com.ritense.authorization.web.rest.request.SearchPermissionsRequest
import com.ritense.valtimo.contract.utils.TestUtil.convertObjectToJsonBytes
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import kotlin.text.Charsets.UTF_8

class PermissionManagementResourceIntTest : BaseIntegrationTest() {

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
    fun `should search permissions`() {
        mockMvc.perform(
            post("/api/management/v1/permissions/search")
                .characterEncoding(UTF_8)
                .content(convertObjectToJsonBytes(SearchPermissionsRequest(listOf("ROLE_USER"))))
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$[0].resourceType", equalTo("com.ritense.authorization.testimpl.TestDocument")))
            .andExpect(jsonPath("$[0].action", equalTo("view")))
            .andExpect(jsonPath("$[0].conditions[0].type", equalTo("field")))
            .andExpect(jsonPath("$[0].conditions[0].field", equalTo("document.name")))
            .andExpect(jsonPath("$[0].conditions[0].operator", equalTo("==")))
            .andExpect(jsonPath("$[0].conditions[0].value", equalTo("loan")))
            .andExpect(jsonPath("$[0].roleKey", equalTo("ROLE_USER")))
    }

}
