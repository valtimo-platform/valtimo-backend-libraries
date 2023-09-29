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

package com.ritense.case.web.rest

import com.ritense.case.BaseIntegrationTest
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

class CaseTabResourceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = [USER])
    fun `should get case tabs`() {
        val caseDefinitionName = "some-case-type"
        mockMvc.perform(
            get("/api/v1/case-definition/{caseDefinitionName}/tab", caseDefinitionName)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Standard"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].key").value("standard"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].type").value("standard"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].content").value("standard"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("Custom tab"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].key").value("custom-tab"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].type").value("custom"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].content").value("some-custom-component"))
    }
}
