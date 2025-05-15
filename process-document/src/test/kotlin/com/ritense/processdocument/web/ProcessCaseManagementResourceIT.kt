/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.processdocument.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.processdocument.BaseIntegrationTest
import com.ritense.processdocument.domain.ProcessDefinitionCaseDefinition
import com.ritense.processdocument.domain.ProcessDefinitionCaseDefinitionId
import com.ritense.processdocument.domain.ProcessDefinitionId
import com.ritense.processdocument.domain.UpdateProcessDefinitionCaseDefinitionRequest
import com.ritense.processdocument.repository.ProcessDefinitionCaseDefinitionRepository
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import java.nio.charset.StandardCharsets

@Transactional
class ProcessCaseManagementResourceIT(
    @Autowired private val webApplicationContext: WebApplicationContext,
    @Autowired private val repository: ProcessDefinitionCaseDefinitionRepository
) : BaseIntegrationTest() {

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build()
    }

    @Test
    fun `should update startableByUser and canInitializeDocument`() {
        val caseDefinitionKey = "example-case"
        val caseDefinitionVersionTag = "1.0.0"
        val processDefinitionId = "process-123"

        val caseDefinitionId = CaseDefinitionId(caseDefinitionKey, caseDefinitionVersionTag)
        val definitionId = ProcessDefinitionCaseDefinitionId(
            ProcessDefinitionId(processDefinitionId),
            caseDefinitionId
        )
        val original = ProcessDefinitionCaseDefinition(
            id = definitionId,
            startableByUser = false,
            canInitializeDocument = false
        )
        repository.save(original)

        val updateRequest = UpdateProcessDefinitionCaseDefinitionRequest(
            startableByUser = true,
            canInitializeDocument = true
        )

        mockMvc.perform(
            put("/api/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/process/{processDefinitionId}/properties",
                caseDefinitionKey, caseDefinitionVersionTag, processDefinitionId
            )
                .content(ObjectMapper().writeValueAsString(updateRequest))
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)

        val updated = repository.findById(definitionId).orElse(null)
        assertNotNull(updated)
        assertEquals(true, updated.startableByUser)
        assertEquals(true, updated.canInitializeDocument)
    }
}