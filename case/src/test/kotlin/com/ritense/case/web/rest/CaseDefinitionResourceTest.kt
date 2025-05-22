package com.ritense.case.web.rest

import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.service.CaseDefinitionService
import com.ritense.case.web.rest.dto.CaseSettingsDto
import com.ritense.exporter.ExportService
import com.ritense.importer.ImportService
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.valtimo.contract.utils.TestUtil
import org.hamcrest.Matchers.nullValue
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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
        val caseDefinitionSettings = CaseDefinitionSettings(
            name = caseDefinitionName(),
            canHaveAssignee = true,
            autoAssignTasks = false,
            hasExternalStartForm = false,
            externalStartFormUrl = null
        )

        whenever(service.getCaseSettings(caseDefinitionName()))
            .thenReturn(caseDefinitionSettings)

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get(
                        CASE_SETTINGS_PATH,
                        caseDefinitionName()
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath(ROOT).isNotEmpty)
            .andExpect(jsonPath(NAME).value(caseDefinitionName()))
            .andExpect(jsonPath("$.canHaveAssignee").value(true))
            .andExpect(jsonPath(AUTO_ASSIGN_TASKS).value(false))
            .andExpect(jsonPath(HAS_EXTERNAL_CREATE_FORM).value(false))
            .andExpect(jsonPath(EXTERNAL_START_FORM_URL, nullValue()))

        verify(service).getCaseSettings(caseDefinitionName())
    }

    @Test
    fun `should update case settings for 'can have assignee'`() {
        val caseDefinitionSettings = CaseDefinitionSettings(
            name = caseDefinitionName(),
            canHaveAssignee = true,
            autoAssignTasks = false
        )
        val caseSettingsDto = CaseSettingsDto(
            canHaveAssignee = false,
            autoAssignTasks = false
        )

        whenever(service.updateCaseSettings(caseDefinitionName(), caseSettingsDto))
            .thenReturn(caseDefinitionSettings)

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .patch(
                        MANAGEMENT_CASE_SETTINGS_PATH,
                        caseDefinitionName()
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(TestUtil.convertObjectToJsonBytes(caseSettingsDto))
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath(ROOT).isNotEmpty)
            .andExpect(jsonPath(NAME).value(caseDefinitionName()))
            .andExpect(jsonPath(CAN_HAVE_ASSIGNEE).value(true))
            .andExpect(jsonPath(AUTO_ASSIGN_TASKS).value(false))
            .andExpect(jsonPath(HAS_EXTERNAL_CREATE_FORM).value(false))
            .andExpect(jsonPath(EXTERNAL_START_FORM_URL, nullValue()))

        verify(service).updateCaseSettings(caseDefinitionName(), caseSettingsDto)
    }

    @Test
    fun `should update case settings for 'has external case start form'`() {
        val externalFormUrl = "https://www.example.com/start-case-form"
        val caseDefinitionSettings = CaseDefinitionSettings(
            name = caseDefinitionName(),
            hasExternalStartForm = true,
            externalStartFormUrl = externalFormUrl
        )
        val caseSettingsDto = CaseSettingsDto(
            hasExternalStartForm = true,
            externalStartFormUrl = externalFormUrl
        )

        whenever(service.updateCaseSettings(caseDefinitionName(), caseSettingsDto))
            .thenReturn(caseDefinitionSettings)

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .patch(
                        MANAGEMENT_CASE_SETTINGS_PATH,
                        caseDefinitionName()
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(TestUtil.convertObjectToJsonBytes(caseSettingsDto))
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath(ROOT).isNotEmpty)
            .andExpect(jsonPath(NAME).value(caseDefinitionName()))
            .andExpect(jsonPath(CAN_HAVE_ASSIGNEE).value(false))
            .andExpect(jsonPath(AUTO_ASSIGN_TASKS).value(false))
            .andExpect(jsonPath(HAS_EXTERNAL_CREATE_FORM).value(true))
            .andExpect(jsonPath(EXTERNAL_START_FORM_URL).value(externalFormUrl))

        verify(service).updateCaseSettings(caseDefinitionName(), caseSettingsDto)
    }

    @Test
    fun `should accept null case settings`() {
        val caseDefinitionSettings = CaseDefinitionSettings(caseDefinitionName())
        val caseSettingsDto = CaseSettingsDto()

        whenever(service.updateCaseSettings(eq(caseDefinitionName()), any()))
            .thenReturn(caseDefinitionSettings)

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .patch(
                        MANAGEMENT_CASE_SETTINGS_PATH,
                        caseDefinitionName()
                    )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(MapperSingleton.get().writeValueAsString(caseSettingsDto))
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath(ROOT).isNotEmpty)
            .andExpect(jsonPath(NAME).value(caseDefinitionName()))

        verify(service).updateCaseSettings(caseDefinitionName(), caseSettingsDto)
    }

    private fun caseDefinitionName() = "name"

    companion object {
        private const val CASE_SETTINGS_PATH = "/api/v1/case/{caseDefinitionName}/settings"
        private const val MANAGEMENT_CASE_SETTINGS_PATH = "/api/management/v1/case/{caseDefinitionName}/settings"

        private const val ROOT = "$"
        private const val NAME = "$.name"
        private const val CAN_HAVE_ASSIGNEE = "$.canHaveAssignee"
        private const val AUTO_ASSIGN_TASKS = "$.autoAssignTasks"
        private const val HAS_EXTERNAL_CREATE_FORM = "$.hasExternalStartForm"
        private const val EXTERNAL_START_FORM_URL = "$.externalStartFormUrl"
    }
}
