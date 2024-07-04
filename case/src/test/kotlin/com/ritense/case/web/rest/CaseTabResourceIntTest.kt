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

package com.ritense.case.web.rest

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case.BaseIntegrationTest
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
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

    @Autowired
    lateinit var documentService: JsonSchemaDocumentService

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = [USER])
    fun `should get case tabs (deprecated)`() {
        val caseDefinitionName = "some-case-type"
        mockMvc.perform(
            get("/api/v1/case-definition/{caseDefinitionName}/tab", caseDefinitionName)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Standard"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].key").value("standard"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].type").value("standard"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].contentKey").value("standard"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("Custom tab"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].key").value("custom-tab"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].type").value("custom"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].contentKey").value("some-custom-component"))
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = [ADMIN])
    fun `should get case tabs filtered for role (deprecated)`() {
        val caseDefinitionName = "some-case-type"
        mockMvc.perform(
            get("/api/v1/case-definition/{caseDefinitionName}/tab", caseDefinitionName)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Custom tab"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].key").value("custom-tab"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].type").value("custom"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].contentKey").value("some-custom-component"))
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = [USER])
    fun `should get case tabs`() {
        val caseDefinitionName = "some-case-type"
        val document = createDocument(caseDefinitionName)
        mockMvc.perform(
            get("/api/v1/document/{documentId}/tab", document.id.id)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Standard"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].key").value("standard"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].type").value("standard"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].contentKey").value("standard"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("Custom tab"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].key").value("custom-tab"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].type").value("custom"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].contentKey").value("some-custom-component"))
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = [ADMIN])
    fun `should get case tabs filtered for role`() {
        val caseDefinitionName = "some-case-type"
        val document = createDocument(caseDefinitionName)
        mockMvc.perform(
            get("/api/v1/document/{documentId}/tab", document.id.id)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Custom tab"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].key").value("custom-tab"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].type").value("custom"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].contentKey").value("some-custom-component"))
    }

    @Test
    @WithMockUser(authorities = ["ROLE_ONLY_TEST_WIDGETS_FOR_CONTEXT"])
    fun `should get case tabs filtered for related document with role`() {
        val caseDefinitionName = "some-case-type"
        val document = createDocument(caseDefinitionName, "{\"key\": \"CONTEXT\"}")
        mockMvc.perform(
            get("/api/v1/document/{documentId}/tab", document.id.id)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Custom tab"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].key").value("custom-tab"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].type").value("custom"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].contentKey").value("some-custom-component"))
    }

    @Test
    @WithMockUser(authorities = ["ROLE_ONLY_TEST_WIDGETS_FOR_CONTEXT"])
    fun `should not get case tabs filtered for related document with role`() {
        val caseDefinitionName = "some-case-type"
        val document = createDocument(caseDefinitionName)
        mockMvc.perform(
            get("/api/v1/document/{documentId}/tab", document.id.id)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty)
    }

    private fun createDocument(documentDefinitionName: String, content: String = "{}"): JsonSchemaDocument {
        return runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    documentDefinitionName,
                    JsonDocumentContent(content).asJson()
                )
            ).resultingDocument().orElseThrow()
        }
    }


}
