package com.ritense.case.web.rest

import com.ritense.case.service.CaseDefinitionService
import com.ritense.case.web.rest.dto.CaseSettingsDto
import com.ritense.case_.domain.definition.CaseDefinition
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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class CaseDefinitionResourceTest {
    lateinit var mockMvc: MockMvc
    lateinit var resource: CaseDefinitionResource
    lateinit var service: CaseDefinitionService
    lateinit var activeCaseDefinitionService: ActiveCaseDefinitionService
    lateinit var exportService: ExportService
    lateinit var importService: ImportService

    @BeforeEach
    fun setUp() {
        service = mock()
        activeCaseDefinitionService = mock()
        exportService = mock()
        importService = mock()
        resource = CaseDefinitionResource(service, activeCaseDefinitionService, exportService, importService)
        mockMvc = MockMvcBuilders.standaloneSetup(resource).build()
    }

    @Test
    fun `should get case settings`() {
        val caseDefinitionId = CaseDefinitionId("key", "1.0.0")
        val caseDefinition = CaseDefinition(caseDefinitionId, "name", true, false, true)

        whenever(activeCaseDefinitionService.getActiveCaseDefinition("key")).thenReturn(caseDefinition)

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get(
                        "/api/v1/case-definition/{caseDefinitionName}/settings",
                        caseDefinitionId.key,
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.caseDefinitionKey").value(caseDefinitionId.key))
            .andExpect(MockMvcResultMatchers.jsonPath("$.caseDefinitionVersionTag")
                .value(caseDefinitionId.versionTag.version))
            .andExpect(MockMvcResultMatchers.jsonPath("$.canHaveAssignee").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.autoAssignTasks").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.active").value(true))

        verify(activeCaseDefinitionService).getActiveCaseDefinition("key")
    }

    @Test
    fun `should update case settings`() {
        val caseDefinitionId = CaseDefinitionId("key", "1.0.0")
        val caseDefinition = CaseDefinition(caseDefinitionId, "name", true, false)
        val caseSettingsDto = CaseSettingsDto(false, false)

        whenever(service.updateCaseSettings(caseDefinitionId, caseSettingsDto)).thenReturn(caseDefinition)

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .patch(
                        "/api/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/settings",
                        caseDefinitionId.key,
                        caseDefinitionId.versionTag
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(TestUtil.convertObjectToJsonBytes(caseSettingsDto))
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.caseDefinitionKey").value(caseDefinitionId.key))
            .andExpect(MockMvcResultMatchers.jsonPath("$.caseDefinitionVersionTag")
                .value(caseDefinitionId.versionTag.version))
            .andExpect(MockMvcResultMatchers.jsonPath("$.canHaveAssignee").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.autoAssignTasks").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.active").value(false))

        verify(service).updateCaseSettings(caseDefinitionId, caseSettingsDto)
    }

    @Test
    fun `should accept null case settings`() {
        val caseDefinitionId = CaseDefinitionId("key", "1.0.0")
        val caseDefinition = CaseDefinition(caseDefinitionId, "name", true, false)
        val caseSettingsDto = CaseSettingsDto()

        whenever(service.updateCaseSettings(eq(caseDefinitionId), any())).thenReturn(caseDefinition)

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .patch(
                        "/api/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/settings",
                        caseDefinitionId.key,
                        caseDefinitionId.versionTag
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(MapperSingleton.get().writeValueAsString(caseSettingsDto))
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.caseDefinitionKey").value(caseDefinitionId.key))
            .andExpect(MockMvcResultMatchers.jsonPath("$.caseDefinitionVersionTag")
                .value(caseDefinitionId.versionTag.version))
            .andExpect(MockMvcResultMatchers.jsonPath("$.canHaveAssignee").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.autoAssignTasks").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.active").value(false))

        verify(service).updateCaseSettings(caseDefinitionId, caseSettingsDto)
    }
}
