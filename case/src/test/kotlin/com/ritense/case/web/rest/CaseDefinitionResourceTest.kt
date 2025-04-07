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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.ritense.case.BaseTest
import com.ritense.case.service.CaseDefinitionService
import com.ritense.case.web.rest.dto.CaseDefinitionDraftCreateRequest
import com.ritense.case.web.rest.dto.CaseSettingsDto
import com.ritense.case_.service.ActiveCaseDefinitionService
import com.ritense.exporter.ExportService
import com.ritense.importer.ImportService
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.valtimo.contract.utils.TestUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class CaseDefinitionResourceTest : BaseTest() {
    lateinit var mockMvc: MockMvc
    lateinit var resource: CaseDefinitionResource
    lateinit var service: CaseDefinitionService
    lateinit var activeCaseDefinitionService: ActiveCaseDefinitionService
    lateinit var exportService: ExportService
    lateinit var importService: ImportService
    lateinit var mapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        service = mock()
        activeCaseDefinitionService = mock()
        exportService = mock()
        importService = mock()
        resource = CaseDefinitionResource(service, activeCaseDefinitionService, exportService, importService)

        mapper = MapperSingleton.get()
        val converter = MappingJackson2HttpMessageConverter()
        converter.objectMapper = mapper

        mockMvc = MockMvcBuilders
            .standaloneSetup(resource)
            .setMessageConverters(converter)
            .build()
    }

    @Test
    fun `should get case settings`() {
        val caseDefinitionId = CaseDefinitionId("key", "1.0.0")
        val caseDefinition = caseDefinition(caseDefinitionId, canHaveAssignee = true)

        whenever(activeCaseDefinitionService.getActiveCaseDefinition("key")).thenReturn(caseDefinition)

        mockMvc
            .perform(
                get(
                    "/api/v1/case-definition/{caseDefinitionName}/settings",
                    caseDefinitionId.key,
                )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$.caseDefinitionKey").value(caseDefinitionId.key))
            .andExpect(jsonPath("$.caseDefinitionVersionTag").value(caseDefinitionId.versionTag.version))
            .andExpect(jsonPath("$.canHaveAssignee").value(true))
            .andExpect(jsonPath("$.autoAssignTasks").value(false))

        verify(activeCaseDefinitionService).getActiveCaseDefinition("key")
    }

    @Test
    fun `should update case settings`() {
        val caseDefinitionId = CaseDefinitionId("key", "1.0.0")
        val caseDefinition = caseDefinition(caseDefinitionId, canHaveAssignee = true)
        val caseSettingsDto = CaseSettingsDto(false, false)

        whenever(service.updateCaseSettings(caseDefinitionId, caseSettingsDto)).thenReturn(caseDefinition)

        mockMvc
            .perform(
                patch(
                    "/api/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/settings",
                    caseDefinitionId.key,
                    caseDefinitionId.versionTag
                )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(TestUtil.convertObjectToJsonBytes(caseSettingsDto))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$.caseDefinitionKey").value(caseDefinitionId.key))
            .andExpect(jsonPath("$.caseDefinitionVersionTag").value(caseDefinitionId.versionTag.version))
            .andExpect(jsonPath("$.canHaveAssignee").value(true))
            .andExpect(jsonPath("$.autoAssignTasks").value(false))

        verify(service).updateCaseSettings(caseDefinitionId, caseSettingsDto)
    }

    @Test
    fun `should accept null case settings`() {
        val caseDefinitionId = CaseDefinitionId("key", "1.0.0")
        val caseDefinition = caseDefinition(caseDefinitionId, canHaveAssignee = true)
        val caseSettingsDto = CaseSettingsDto()

        whenever(service.updateCaseSettings(eq(caseDefinitionId), any())).thenReturn(caseDefinition)

        mockMvc
            .perform(
                patch(
                    "/api/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/settings",
                    caseDefinitionId.key,
                    caseDefinitionId.versionTag
                )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(MapperSingleton.get().writeValueAsString(caseSettingsDto))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$.caseDefinitionKey").value(caseDefinitionId.key))
            .andExpect(jsonPath("$.caseDefinitionVersionTag").value(caseDefinitionId.versionTag.version))
            .andExpect(jsonPath("$.canHaveAssignee").value(true))
            .andExpect(jsonPath("$.autoAssignTasks").value(false))

        verify(service).updateCaseSettings(caseDefinitionId, caseSettingsDto)
    }

    @Test
    fun `should get case definition`() {
        val caseDefinitionId = CaseDefinitionId("key", "1.0.0")
        val caseDefinition = caseDefinition(caseDefinitionId)
        whenever(service.getCaseDefinition(eq(caseDefinitionId))).thenReturn(caseDefinition)

        mockMvc.perform(
            get(
                "/api/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}",
                caseDefinitionId.key,
                caseDefinitionId.versionTag
            )
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.caseDefinitionKey").value(caseDefinition.id.key))
            .andExpect(jsonPath("$.caseDefinitionVersionTag").value(caseDefinition.id.versionTag.version))
            .andExpect(jsonPath("$.name").value(caseDefinition.name))
            .andExpect(jsonPath("$.description").value(caseDefinition.description))
            .andExpect(jsonPath("$.createdBy").value(caseDefinition.createdBy))
            .andExpect(jsonPath("$.createdDate").value(mapper.convertValue<String>(caseDefinition.createdDate!!)))
            .andExpect(jsonPath("$.baseOnVersionTag").value(caseDefinition.baseOnVersionTag?.version))
            .andExpect(jsonPath("$.isFinal").value(caseDefinition.isFinal))
            .andExpect(jsonPath("$.canHaveAssignee").value(caseDefinition.canHaveAssignee))
            .andExpect(jsonPath("$.autoAssignTasks").value(caseDefinition.autoAssignTasks))
    }

    @Test
    fun `should create case definition draft`() {
        val caseDefinitionId = CaseDefinitionId("key", "1.0.0")
        val caseDefinition = caseDefinition(caseDefinitionId)
        val request = CaseDefinitionDraftCreateRequest(
            versionTag = "1.0.0",
            description = "description",
        )
        whenever(service.createCaseDefinitionDraft(eq(caseDefinitionId), eq(request))).thenReturn(caseDefinition)

        mockMvc.perform(
            post(
                "/api/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/draft",
                caseDefinitionId.key,
                caseDefinitionId.versionTag
            )
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(MapperSingleton.get().writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.caseDefinitionKey").value(caseDefinition.id.key))
            .andExpect(jsonPath("$.caseDefinitionVersionTag").value(caseDefinition.id.versionTag.version))
            .andExpect(jsonPath("$.name").value(caseDefinition.name))
            .andExpect(jsonPath("$.description").value(caseDefinition.description))
            .andExpect(jsonPath("$.createdBy").value(caseDefinition.createdBy))
            .andExpect(jsonPath("$.createdDate").value(mapper.convertValue<String>(caseDefinition.createdDate!!)))
            .andExpect(jsonPath("$.baseOnVersionTag").value(caseDefinition.baseOnVersionTag?.version))
            .andExpect(jsonPath("$.isFinal").value(caseDefinition.isFinal))
            .andExpect(jsonPath("$.canHaveAssignee").value(caseDefinition.canHaveAssignee))
            .andExpect(jsonPath("$.autoAssignTasks").value(caseDefinition.autoAssignTasks))
    }

    @Test
    fun `should delete case definition draft`() {
        val caseDefinitionId = CaseDefinitionId("key", "1.0.0")

        mockMvc.perform(
            delete(
                "/api/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}",
                caseDefinitionId.key,
                caseDefinitionId.versionTag
            )
        )
            .andExpect(status().isOk)

        verify(service).deleteCaseDefinition(caseDefinitionId)
    }
}
