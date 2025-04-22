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
import com.ritense.BaseTest
import com.ritense.case.service.CaseDefinitionService
import com.ritense.case.web.rest.dto.CaseDefinitionDraftCreateRequest
import com.ritense.case.web.rest.dto.CaseDefinitionUpdateRequest
import com.ritense.case.web.rest.dto.CaseSettingsDto
import com.ritense.case_.repository.CaseDefinitionRepository
import com.ritense.case_.service.ActiveCaseDefinitionService
import com.ritense.exporter.ExportService
import com.ritense.importer.ImportService
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.valtimo.contract.utils.TestUtil
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
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

//TODO: use the constants in every test for URL and json paths
class CaseDefinitionResourceTest : BaseTest() {

    lateinit var mockMvc: MockMvc
    lateinit var resource: CaseDefinitionResource
    lateinit var service: CaseDefinitionService
    lateinit var activeCaseDefinitionService: ActiveCaseDefinitionService
    lateinit var exportService: ExportService
    lateinit var importService: ImportService
    lateinit var caseDefinitionRepository: CaseDefinitionRepository
    lateinit var mapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        service = mock()
        activeCaseDefinitionService = mock()
        exportService = mock()
        importService = mock()
        caseDefinitionRepository = mock()
        resource = CaseDefinitionResource(
            service,
            activeCaseDefinitionService,
            exportService,
            importService,
            caseDefinitionRepository
        )

        mapper = MapperSingleton.get()
        val converter = MappingJackson2HttpMessageConverter()
        converter.objectMapper = mapper

        mockMvc = MockMvcBuilders
            .standaloneSetup(resource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(converter)
            .build()
    }

    @Test
    fun `should get case settings`() {
        val caseDefinition = caseDefinition(
            canHaveAssignee = true,
            autoAssignTasks = false
        )

        whenever(activeCaseDefinitionService.getActiveCaseDefinition("key"))
            .thenReturn(caseDefinition)

        mockMvc
            .perform(
                get(
                    "/api/v1/case-definition/{caseDefinitionName}/settings",
                    caseDefinition.id.key,
                )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath(ROOT).isNotEmpty)
            .andExpect(jsonPath("$.caseDefinitionKey").value(caseDefinition.id.key))
            .andExpect(jsonPath("$.caseDefinitionVersionTag").value(caseDefinition.id.versionTag.version))
            .andExpect(jsonPath("$.canHaveAssignee").value(true))
            .andExpect(jsonPath(AUTO_ASSIGN_TASKS).value(false))
            .andExpect(jsonPath(HAS_EXTERNAL_CREATE_FORM).value(false))
            .andExpect(jsonPath(EXTERNAL_START_FORM_URL, nullValue()))

        verify(activeCaseDefinitionService).getActiveCaseDefinition("key")
    }

    @Test
    fun `should update case settings`() {
        val caseDefinition = caseDefinition(
            canHaveAssignee = true,
            autoAssignTasks = false
        )
        val caseSettingsDto = CaseSettingsDto(
            canHaveAssignee = false,
            autoAssignTasks = false
        )

        whenever(service.updateCaseSettings(caseDefinition.id, caseSettingsDto))
            .thenReturn(caseDefinition)

        mockMvc
            .perform(
                patch(
                    "/api/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/settings",
                    caseDefinition.id.key,
                    caseDefinition.id.versionTag
                )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(TestUtil.convertObjectToJsonBytes(caseSettingsDto))
            )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$.caseDefinitionKey").value(caseDefinition.id.key))
            .andExpect(jsonPath("$.caseDefinitionVersionTag").value(caseDefinition.id.versionTag.version))
            .andExpect(jsonPath(CAN_HAVE_ASSIGNEE).value(true))
            .andExpect(jsonPath(AUTO_ASSIGN_TASKS).value(false))
            .andExpect(jsonPath(HAS_EXTERNAL_CREATE_FORM).value(false))
            .andExpect(jsonPath(EXTERNAL_START_FORM_URL, nullValue()))

        verify(service).updateCaseSettings(caseDefinition.id, caseSettingsDto)
    }

    @Test
    fun `should update case settings for 'has external case start form'`() {
        val externalFormUrl = "https://www.example.com/start-case-form"
        val caseDefinition = caseDefinition(
            hasExternalStartForm = true,
            externalStartFormUrl = externalFormUrl
        )
        val caseSettingsDto = CaseSettingsDto(
            hasExternalStartForm = true,
            externalStartFormUrl = externalFormUrl
        )

        whenever(service.updateCaseSettings(caseDefinition.id, caseSettingsDto))
            .thenReturn(caseDefinition)

        mockMvc
            .perform(
                patch(
                    "/api/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/settings",
                    caseDefinition.id.key,
                    caseDefinition.id.versionTag
                )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(TestUtil.convertObjectToJsonBytes(caseSettingsDto))
            )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath(ROOT).isNotEmpty)
            .andExpect(jsonPath(CAN_HAVE_ASSIGNEE).value(false))
            .andExpect(jsonPath(AUTO_ASSIGN_TASKS).value(false))
            .andExpect(jsonPath(HAS_EXTERNAL_CREATE_FORM).value(true))
            .andExpect(jsonPath(EXTERNAL_START_FORM_URL).value(externalFormUrl))

        verify(service).updateCaseSettings(caseDefinition.id, caseSettingsDto)
    }


    @Test
    fun `should accept null case settings`() {
        val caseDefinitionId = CaseDefinitionId("key", "1.0.0")
        val caseDefinition = caseDefinition(caseDefinitionId, canHaveAssignee = true, active = false)
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
            .andExpect(jsonPath("$.basedOnVersionTag").value(caseDefinition.basedOnVersionTag?.version))
            .andExpect(jsonPath("$.final").value(caseDefinition.final))
            .andExpect(jsonPath("$.canHaveAssignee").value(caseDefinition.canHaveAssignee))
            .andExpect(jsonPath("$.autoAssignTasks").value(caseDefinition.autoAssignTasks))
    }

    @Test
    fun `should create case definition draft`() {
        val caseDefinitionId = CaseDefinitionId("key", "1.0.0")
        val caseDefinition = caseDefinition(caseDefinitionId)
        val request = CaseDefinitionDraftCreateRequest(
            caseDefinitionKey = caseDefinition.id.key,
            caseDefinitionVersion = caseDefinition.id.versionTag.toString(),
            name = "name",
            description = "description",
            basedOnCaseDefinitionVersion = "1.0.0-SNAPSHOT"
        )
        whenever(service.createCaseDefinitionDraft(eq(request))).thenReturn(caseDefinition)

        mockMvc.perform(
            post(
                "/api/management/v1/case-definition/draft",
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
            .andExpect(jsonPath("$.basedOnVersionTag").value(caseDefinition.basedOnVersionTag?.version))
            .andExpect(jsonPath("$.final").value(caseDefinition.final))
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

    @Test
    fun `should get case definitions`() {
        val caseDefinitionId = CaseDefinitionId("key", "1.0.0")
        val caseDefinition = caseDefinition(caseDefinitionId)
        whenever(service.getCaseDefinitions(isNull(), isNull(), any())).thenReturn(PageImpl(listOf(caseDefinition)))

        mockMvc.perform(
            get("/api/management/v1/case-definition")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].caseDefinitionKey").value(caseDefinition.id.key))
            .andExpect(jsonPath("$.content[0].caseDefinitionVersionTag").value(caseDefinition.id.versionTag.version))
            .andExpect(jsonPath("$.content[0].name").value(caseDefinition.name))
            .andExpect(jsonPath("$.content[0].canHaveAssignee").value(caseDefinition.canHaveAssignee))
            .andExpect(jsonPath("$.content[0].autoAssignTasks").value(caseDefinition.autoAssignTasks))
            .andExpect(jsonPath("$.content[0].active").value(caseDefinition.active))

    }

    @Test
    fun `should get case definition versions`() {
        val caseDefinitionId = CaseDefinitionId("key", "1.0.0")
        val caseDefinition = caseDefinition(caseDefinitionId, "name", true, false)
        whenever(service.getCaseDefinitions(eq(caseDefinitionId.key), isNull(), any())).thenReturn(
            PageImpl(
                listOf(
                    caseDefinition
                )
            )
        )

        mockMvc.perform(
            get(
                "/api/management/v1/case-definition/{caseDefinitionKey}/version",
                caseDefinitionId.key
            )
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].versionTag").value(caseDefinition.id.versionTag.version))
            .andExpect(jsonPath("$[0].active").value(caseDefinition.active))
    }

    @Test
    fun `should finalize case definition`() {
        val caseDefinition = caseDefinition()
        whenever(service.finalizeCaseDefinition(caseDefinition.id)).thenReturn(caseDefinition)

        mockMvc.perform(
            post(
                "/api/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/finalize",
                caseDefinition.id.key,
                caseDefinition.id.versionTag,
            )
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.final").value(true))
    }

    @Test
    fun `should update case definition`() {
        val request = CaseDefinitionUpdateRequest(
            name = "name",
            description = "description",
        )
        val caseDefinition = caseDefinition()
        whenever(service.updateCaseDefinition(caseDefinition.id, request.name, request.description))
            .thenReturn(caseDefinition)

        mockMvc.perform(
            patch(
                "/api/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}",
                caseDefinition.id.key,
                caseDefinition.id.versionTag,
            )
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(MapperSingleton.get().writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value(caseDefinition.name))
            .andExpect(jsonPath("$.description").value(caseDefinition.description))
    }

    companion object {
        private const val CASE_SETTINGS_PATH =
            "/api/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/settings"
        private const val MANAGEMENT_CASE_SETTINGS_PATH = "/api/management/v1/case/{caseDefinitionName}/settings"

        private const val ROOT = "$"
        private const val NAME = "$.name"
        private const val CAN_HAVE_ASSIGNEE = "$.canHaveAssignee"
        private const val AUTO_ASSIGN_TASKS = "$.autoAssignTasks"
        private const val HAS_EXTERNAL_CREATE_FORM = "$.hasExternalStartForm"
        private const val EXTERNAL_START_FORM_URL = "$.externalStartFormUrl"
    }
}
