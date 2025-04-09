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
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessRequest
import com.ritense.processdocument.service.CaseDefinitionProcessLinkService
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import java.nio.charset.StandardCharsets

@Transactional
class CaseDefinitionProcessManagementResourceIT(
    @Autowired private val caseDefinitionProcessLinkService: CaseDefinitionProcessLinkService,
    @Autowired private val webApplicationContext: WebApplicationContext
): BaseIntegrationTest() {

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun init() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()
    }

    @Test
    fun `should get a feature process`() {
        caseDefinitionProcessLinkService.saveDocumentDefinitionProcess(
            CaseDefinitionId("house", "1.0.0"),
            DocumentDefinitionProcessRequest(
                "unassociated-process",
                "DOCUMENT_UPLOAD"
            )
        )

        mockMvc.perform(
            get("/api/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/feature-process/{type}", "house", "1.0.0", "DOCUMENT_UPLOAD")
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.processDefinitionKey").value("unassociated-process"))
        .andExpect(jsonPath("$.processName").value("Unassociated process"))
    }

    @Test
    fun `should create a feature process`() {
        val request = DocumentDefinitionProcessRequest(
            "unassociated-process",
            "test"
        )

        mockMvc.perform(
            put("/api/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/feature-process", "house", "1.0.0")
                .content(ObjectMapper().writeValueAsString(request))
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk)


        val documentDefinitionProcess = caseDefinitionProcessLinkService.getDocumentDefinitionProcess(
            CaseDefinitionId("house", "1.0.0"),
            "test"
        )

        assertNotNull(documentDefinitionProcess)
    }

    @Test
    fun `should delete a feature process`() {
        caseDefinitionProcessLinkService.saveDocumentDefinitionProcess(
            CaseDefinitionId("house", "1.0.0"),
            DocumentDefinitionProcessRequest(
                "unassociated-process",
                "other"
            )
        )

        mockMvc.perform(
            delete("/api/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/feature-process/{type}", "house", "1.0.0", "other")
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk)

        val documentDefinitionProcess = caseDefinitionProcessLinkService.getDocumentDefinitionProcess(
            CaseDefinitionId("house", "1.0.0"),
            "other"
        )

        assertNull(documentDefinitionProcess)
    }
}