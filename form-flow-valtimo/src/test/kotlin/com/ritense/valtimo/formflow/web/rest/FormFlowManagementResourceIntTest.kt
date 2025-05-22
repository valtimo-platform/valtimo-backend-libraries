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

package com.ritense.valtimo.formflow.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.service.FormFlowService
import com.ritense.valtimo.formflow.BaseIntegrationTest
import com.ritense.valtimo.formflow.web.rest.result.FormFlowDefinitionDto
import jakarta.ws.rs.core.MediaType.APPLICATION_JSON
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@Transactional
class FormFlowManagementResourceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    lateinit var formFlowService: FormFlowService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun init() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()
    }

    @Test
    fun `should return form flow definitions`() {
        mockMvc
            .perform(get("/api/management/v1/form-flow/definition"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.key=='inkomens_loket')].key").value("inkomens_loket"))
            .andExpect(jsonPath("$.content[?(@.key=='inkomens_loket')].readOnly").value(true))
            .andExpect(jsonPath("$.content[?(@.key=='inkomens_loket')].versions[0]").value(1))
    }

    @Test
    fun `should return form flow definition by id`() {
        mockMvc
            .perform(get("/api/management/v1/form-flow/definition/{key}/{version}", "inkomens_loket", 1))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value("inkomens_loket"))
            .andExpect(jsonPath("$.version").value(1))
            .andExpect(jsonPath("$.startStep").value("woonplaats"))
            .andExpect(jsonPath("$.steps").exists())
    }

    @Test
    fun `should delete form flow definition by key`() {
        formFlowService.save(FormFlowDefinition(FormFlowDefinitionId("test", 1), "start-step", setOf()))
        mockMvc
            .perform(delete("/api/management/v1/form-flow/definition/{key}", "test"))
            .andDo(print())
            .andExpect(status().isOk)
    }

    @Test
    fun `should create form flow definition`() {
        val definition = FormFlowDefinitionDto(
            key = "test",
            version = 1,
            startStep = "start-step",
            steps = listOf()
        )

        mockMvc.perform(
            post("/api/management/v1/form-flow/definition")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(definition))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value("test"))
            .andExpect(jsonPath("$.version").value(1))
            .andExpect(jsonPath("$.startStep").value("start-step"))
            .andExpect(jsonPath("$.steps").exists())
    }

    @Test
    fun `should update form flow definition`() {
        formFlowService.save(FormFlowDefinition(FormFlowDefinitionId("test", 1), "start-step", setOf()))

        val definition = FormFlowDefinitionDto(
            key = "test",
            version = 2,
            startStep = "start-step-changed",
            steps = listOf()
        )

        mockMvc.perform(
            put("/api/management/v1/form-flow/definition/{key}", "test")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(definition))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value("test"))
            .andExpect(jsonPath("$.version").value(2))
            .andExpect(jsonPath("$.startStep").value("start-step-changed"))
            .andExpect(jsonPath("$.steps").exists())
    }

}
