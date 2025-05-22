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
import org.hamcrest.Matchers.hasSize
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
    fun `should get list of ALL possible value resolvers when prefix is empty`() {

        val prefixes = """[]"""

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
    fun `should get list of ALL possible value resolvers when prefixes is empty v2`() {

        val prefixes = """{"prefixes":[],"type":"FIELD"}"""

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/management/v2/value-resolver/document-definition/{documentDefinitionName}/keys", "x")
                .contentType(APPLICATION_JSON_UTF8)
                .content(prefixes)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].path").value("testDoc:1"))
            .andExpect(jsonPath("$[0].type").value("FIELD"))
            .andExpect(jsonPath("$[1].path").value("testDoc:2"))
            .andExpect(jsonPath("$[1].type").value("FIELD"))
            .andExpect(jsonPath("$[2].path").value("testDoc:3"))
            .andExpect(jsonPath("$[2].type").value("FIELD"))
    }

    @Test
    fun `should get list of possible value resolvers for a single prefix v2`() {

        val prefixes = """{"prefixes":["testDoc"],"type":"FIELD"}"""

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/management/v2/value-resolver/document-definition/{documentDefinitionName}/keys", "x")
                .contentType(APPLICATION_JSON_UTF8)
                .content(prefixes)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].path").value("testDoc:1"))
            .andExpect(jsonPath("$[0].type").value("FIELD"))
            .andExpect(jsonPath("$[1].path").value("testDoc:2"))
            .andExpect(jsonPath("$[1].type").value("FIELD"))
            .andExpect(jsonPath("$[2].path").value("testDoc:3"))
            .andExpect(jsonPath("$[2].type").value("FIELD"))
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
    fun `should get list of possible value resolvers for multiple prefixes v2`() {

        val prefixes = """{"prefixes":["testDoc", "testCase"],"type":"FIELD"}"""

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/management/v2/value-resolver/document-definition/{documentDefinitionName}/keys", "x")
                .contentType(APPLICATION_JSON_UTF8)
                .content(prefixes)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].path").value("testDoc:1"))
            .andExpect(jsonPath("$[0].type").value("FIELD"))
            .andExpect(jsonPath("$[1].path").value("testDoc:2"))
            .andExpect(jsonPath("$[1].type").value("FIELD"))
            .andExpect(jsonPath("$[2].path").value("testDoc:3"))
            .andExpect(jsonPath("$[2].type").value("FIELD"))
            .andExpect(jsonPath("$[3].path").value("testCase:4"))
            .andExpect(jsonPath("$[3].type").value("FIELD"))
            .andExpect(jsonPath("$[4].path").value("testCase:5"))
            .andExpect(jsonPath("$[4].type").value("FIELD"))
            .andExpect(jsonPath("$[5].path").value("testCase:6"))
            .andExpect(jsonPath("$[5].type").value("FIELD"))
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

    @Test
    fun `should return empty list when requesting non-existent value resolver prefixes v2`() {

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

    @Test
    fun `should get list of collection value resolvers for multiple prefixes v2`() {

        val prefixes = """{"prefixes":["testCase"],"type":"COLLECTION"}"""

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/management/v2/value-resolver/document-definition/{documentDefinitionName}/keys", "x")
                .contentType(APPLICATION_JSON_UTF8)
                .content(prefixes)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].path").value("test"))
            .andExpect(jsonPath("$[0].type").value("COLLECTION"))
            .andExpect(jsonPath("$[0].children.*", hasSize<Int>(3)))
            .andExpect(jsonPath("$[0].children[0].path").value("1"))
            .andExpect(jsonPath("$[0].children[0].type").value("FIELD"))
            .andExpect(jsonPath("$[0].children[1].path").value("2"))
            .andExpect(jsonPath("$[0].children[1].type").value("FIELD"))
            .andExpect(jsonPath("$[0].children[2].path").value("3"))
            .andExpect(jsonPath("$[0].children[2].type").value("FIELD"))
    }
}