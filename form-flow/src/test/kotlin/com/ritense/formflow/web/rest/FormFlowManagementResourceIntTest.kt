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

package com.ritense.formflow.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.service.FormFlowService
import com.ritense.formflow.BaseIntegrationTest
import com.ritense.formflow.web.rest.result.FormFlowDefinitionDto
import com.ritense.valtimo.contract.case_.CaseDefinitionId
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
            .perform(get("/api/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/form-flow-definition", "profile", "1.0.0"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.key=='inkomens_loket')].key").value("inkomens_loket"))
            .andExpect(jsonPath("$.content[?(@.key=='inkomens_loket')].readOnly").value(true))
    }

    @Test
    fun `should return form flow definition by id`() {
        mockMvc
            .perform(get("/api/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/form-flow-definition/{definitionKey}", "profile", "1.0.0", "inkomens_loket", 1))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value("inkomens_loket"))
            .andExpect(jsonPath("$.startStep").value("woonplaats"))
            .andExpect(jsonPath("$.steps").exists())
    }

    @Test
    fun `should delete form flow definition by key`() {
        val caseDefinitionId = CaseDefinitionId("profile", "1.0.0")
        formFlowService.save(FormFlowDefinition(FormFlowDefinitionId("test", caseDefinitionId), "start-step", setOf()))
        mockMvc
            .perform(delete("/api/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/form-flow-definition/{definitionKey}", "profile", "1.0.0", "test"))
            .andDo(print())
            .andExpect(status().isOk)
    }

    @Test
    fun `should create form flow definition`() {
        val definition = FormFlowDefinitionDto(
            key = "test",
            startStep = "start-step",
            steps = listOf()
        )

        mockMvc.perform(
            post("/api/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/form-flow-definition", "profile", "1.0.0")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(definition))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value("test"))
            .andExpect(jsonPath("$.startStep").value("start-step"))
            .andExpect(jsonPath("$.steps").exists())
    }

    @Test
    fun `should update form flow definition`() {
        val caseDefinitionId = CaseDefinitionId("profile", "1.0.0")
        formFlowService.save(FormFlowDefinition(FormFlowDefinitionId("test", caseDefinitionId), "start-step", setOf()))

        val definition = FormFlowDefinitionDto(
            key = "test",
            startStep = "start-step-changed",
            steps = listOf()
        )

        mockMvc.perform(
            put("/api/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/form-flow-definition/{definitionKey}", "profile", "1.0.0", "test")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(definition))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value("test"))
            .andExpect(jsonPath("$.startStep").value("start-step-changed"))
            .andExpect(jsonPath("$.steps").exists())
    }

}
