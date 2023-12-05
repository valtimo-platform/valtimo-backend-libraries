package com.ritense.case.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.service.CaseDefinitionService
import com.ritense.case.web.rest.dto.CaseSettingsDto
import com.ritense.export.ExportService
import com.ritense.import.ImportService
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
    lateinit var exportService: ExportService
    lateinit var importService: ImportService

    @BeforeEach
    fun setUp() {
        service = mock()
        exportService = mock()
        importService = mock()
        resource = CaseDefinitionResource(service, exportService, importService)
        mockMvc = MockMvcBuilders.standaloneSetup(resource).build()
    }

    @Test
    fun `should get case settings`() {
        val caseDefinitionName = "name"
        val caseDefinitionSettings = CaseDefinitionSettings(caseDefinitionName, true, false)

        whenever(service.getCaseSettings(caseDefinitionName)).thenReturn(caseDefinitionSettings)

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get(
                        "/api/v1/case/{caseDefinitionName}/settings",
                        caseDefinitionName
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(caseDefinitionName))
            .andExpect(MockMvcResultMatchers.jsonPath("$.canHaveAssignee").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.autoAssignTasks").value(false))

        verify(service).getCaseSettings(caseDefinitionName)
    }

    @Test
    fun `should update case settings`() {
        val caseDefinitionName = "name"
        val caseDefinitionSettings = CaseDefinitionSettings(caseDefinitionName, true, false)
        val caseSettingsDto = CaseSettingsDto(false,false)

        whenever(service.updateCaseSettings(caseDefinitionName, caseSettingsDto)).thenReturn(caseDefinitionSettings)

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .patch(
                        "/api/v1/case/{caseDefinitionName}/settings",
                        caseDefinitionName
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(TestUtil.convertObjectToJsonBytes(caseSettingsDto))
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(caseDefinitionName))
            .andExpect(MockMvcResultMatchers.jsonPath("$.canHaveAssignee").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.autoAssignTasks").value(false))

        verify(service).updateCaseSettings(caseDefinitionName, caseSettingsDto)
    }

    @Test
    fun `should accept null case settings`() {
        val caseDefinitionName = "name"
        val caseDefinitionSettings = CaseDefinitionSettings(caseDefinitionName)
        val caseSettingsDto = CaseSettingsDto()

        whenever(service.updateCaseSettings(eq(caseDefinitionName), any())).thenReturn(caseDefinitionSettings)

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .patch(
                        "/api/v1/case/{caseDefinitionName}/settings",
                        caseDefinitionName
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(jacksonObjectMapper().writeValueAsString(caseSettingsDto))
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(caseDefinitionName))

        verify(service).updateCaseSettings(caseDefinitionName, caseSettingsDto)
    }
}
