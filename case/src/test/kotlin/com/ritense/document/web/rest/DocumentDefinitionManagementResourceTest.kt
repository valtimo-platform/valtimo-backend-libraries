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

package com.ritense.document.web.rest

import com.ritense.document.BaseTest
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.document.service.request.DocumentDefinitionCreateRequest
import com.ritense.document.service.result.DeployDocumentDefinitionResult
import com.ritense.document.service.result.DeployDocumentDefinitionResultFailed
import com.ritense.document.service.result.DeployDocumentDefinitionResultSucceeded
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valtimo.contract.json.MapperSingleton.get
import com.ritense.valtimo.contract.result.OperationError
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.nio.charset.StandardCharsets
import java.util.Optional

class DocumentDefinitionManagementResourceTest : BaseTest() {

    private lateinit var documentDefinitionService: JsonSchemaDocumentDefinitionService
    private lateinit var documentDefinitionManagementResource: DocumentDefinitionManagementResource
    private lateinit var mockMvc: MockMvc
    private lateinit var definitionPage: Page<JsonSchemaDocumentDefinition>
    private lateinit var definition: JsonSchemaDocumentDefinition

    @BeforeEach
    fun setUp() {
        documentDefinitionService = mock()

        documentDefinitionManagementResource = DocumentDefinitionManagementResource(
            documentDefinitionService
        )

        mockMvc = MockMvcBuilders.standaloneSetup(documentDefinitionManagementResource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(MappingJackson2HttpMessageConverter(get()))
            .build()

        definition = definition()
        val definitions = listOf(definition)
        val unpaged = Pageable.unpaged()

        definitionPage = PageImpl<JsonSchemaDocumentDefinition>(definitions, unpaged, 1)
    }

    @Test
    fun shouldReturnSingleDefinitionRecordByNameAndVersion() {
        val caseDefinitionId = definition.getId().caseDefinitionId()
        whenever(documentDefinitionService.findByCaseDefinitionId(caseDefinitionId))
            .thenReturn(Optional.of(definition))
        mockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/document-definition",
                caseDefinitionId.key,
                caseDefinitionId.versionTag.getVersion()
            )
        )
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(APPLICATION_JSON_UTF8_VALUE))
        .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty())
    }

    @Test
    fun shouldReturnNoDefinitionRecordByNameAndVersion() {
        val caseDefinitionId = definition.getId().caseDefinitionId()
        whenever(documentDefinitionService.findByCaseDefinitionId(caseDefinitionId))
            .thenReturn(Optional.empty<JsonSchemaDocumentDefinition>())
        mockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/document-definition",
                caseDefinitionId.key,
                caseDefinitionId.versionTag.getVersion()
            )
        )
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.status().isNotFound())
    }


    @Test
    fun shouldReturnCreateSuccessResult() {
        val objectMapper = get()
        val documentDefinitionCreateRequest = DocumentDefinitionCreateRequest(
            "{\n" +
                "  \"\$id\": \"person.schema\",\n" +
                "  \"\$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                "  \"title\": \"Person\",\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"firstName\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"The person's first name.\"\n" +
                "    },\n" +
                "    \"lastName\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"The person's last name.\"\n" +
                "    },\n" +
                "    \"age\": {\n" +
                "      \"description\": \"Age in years which must be equal to or greater than zero.\",\n" +
                "      \"type\": \"integer\",\n" +
                "      \"minimum\": 0\n" +
                "    }\n" +
                "  }\n" +
                "}\n"
        )

        whenever(
            documentDefinitionService.deploy(
                anyString(),
                any<CaseDefinitionId>()
            )
        )
            .thenReturn(DeployDocumentDefinitionResultSucceeded(definition))

        mockMvc.perform(
            MockMvcRequestBuilders.put(
                "/api/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/document-definition",
                "caseDefinitionId",
                "1.0.0"
            )
            .content(objectMapper.writeValueAsString(documentDefinitionCreateRequest))
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.status().isOk())

        verify(documentDefinitionService, Mockito.times(1))
            .deploy(anyString(), any<CaseDefinitionId>())
    }

    @Test
    fun shouldReturnCreateFailedResult() {
        val objectMapper = get()
        val documentDefinitionCreateRequest = DocumentDefinitionCreateRequest(
            "{\n" +
                "  \"\$id\": \"person.schema\",\n" +
                "  \"\$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                "  \"title\": \"Person\",\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"firstName\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"The person's first name.\"\n" +
                "    },\n" +
                "    \"lastName\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"The person's last name.\"\n" +
                "    },\n" +
                "    \"age\": {\n" +
                "      \"description\": \"Age in years which must be equal to or greater than zero.\",\n" +
                "      \"type\": \"integer\",\n" +
                "      \"minimum\": 0\n" +
                "    }\n" +
                "  }\n" +
                "}\n"
        )

        whenever<DeployDocumentDefinitionResult?>(
            documentDefinitionService.deploy(
                anyString(),
                any<CaseDefinitionId>()
            )
        )
        .thenReturn(DeployDocumentDefinitionResultFailed(listOf(OperationError { "This schema was already deployed" })))

        mockMvc.perform(
            MockMvcRequestBuilders.put(
                "/api/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/document-definition",
                "caseDefinitionId",
                "1.0.0"
            )
            .content(objectMapper.writeValueAsString(documentDefinitionCreateRequest))
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.status().isBadRequest())

       verify(documentDefinitionService, Mockito.times(1))
            .deploy(anyString(), any<CaseDefinitionId>())
    }

}