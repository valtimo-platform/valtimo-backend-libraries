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

package com.ritense.valueresolver.web.rest

import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8
import com.ritense.valueresolver.BaseIntegrationTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebAppConfiguration
class ValueResolverResourceIT @Autowired constructor(
    private val webApplicationContext: WebApplicationContext,
) : BaseIntegrationTest() {
    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build()
    }

    @Test
    fun `should get list of possible value resolvers for a single prefix`() {

        val prefixes = """["testDoc"]"""

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/management/v1/value-resolver/document-definition/{documentDefinitionName}/keys", "x")
                .contentType(APPLICATION_JSON_UTF8)
                .content(prefixes)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0]").value("testDoc:1"))
            .andExpect(jsonPath("$[1]").value("testDoc:2"))
            .andExpect(jsonPath("$[2]").value("testDoc:3"))
    }

    @Test
    fun `should get list of possible value resolvers for multiple prefixes`() {

        val prefixes = """["testDoc", "testCase"]"""

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/management/v1/value-resolver/document-definition/{documentDefinitionName}/keys", "x")
                .contentType(APPLICATION_JSON_UTF8)
                .content(prefixes)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0]").value("testDoc:1"))
            .andExpect(jsonPath("$[1]").value("testDoc:2"))
            .andExpect(jsonPath("$[2]").value("testDoc:3"))
            .andExpect(jsonPath("$[3]").value("testCase:4"))
            .andExpect(jsonPath("$[4]").value("testCase:5"))
            .andExpect(jsonPath("$[5]").value("testCase:6"))
    }

    @Test
    fun `should return empty list when requesting non-existent value resolver prefixes`() {

        val prefixes = """["nonExistent"]"""

        val result = mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/management/v1/value-resolver/document-definition/{documentDefinitionName}/keys", "x")
                .contentType(APPLICATION_JSON_UTF8)
                .content(prefixes)
        )
            .andDo(print())
            .andExpect(status().isOk)

        Assertions.assertThat(result.andReturn().response.contentAsString).isEqualTo("[]")
    }
}