package com.ritense.case.web.dto

import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.web.rest.dto.CaseSettingsDto
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CaseSettingsDtoTest {
    @Test
    fun `should update case settings when value is not null`() {
        val currentCaseSettings = CaseDefinitionSettings("name")
        val caseSettingsDto = CaseSettingsDto(true)
        val updatedCaseSettings = caseSettingsDto.update(currentCaseSettings)

        assertFalse(currentCaseSettings.canHaveAssignee)
        assertTrue(caseSettingsDto.canHaveAssignee!!)
        assertTrue(updatedCaseSettings.canHaveAssignee)
    }

    @Test
    fun `should not update case settings when value is null`() {
        val currentCaseSettings = CaseDefinitionSettings("name")
        val caseSettingsDto = CaseSettingsDto()
        val updatedCaseSettings = caseSettingsDto.update(currentCaseSettings)


        assertFalse(currentCaseSettings.canHaveAssignee)
        assertNull(caseSettingsDto.canHaveAssignee)
        assertFalse(updatedCaseSettings.canHaveAssignee)
    }

    @Test
    fun `should set autoAssignTasks to false when canHaveAssignee is set to false`() {
        val currentCaseSettings = CaseDefinitionSettings(
            name = "case-name",
            canHaveAssignee = true,
            autoAssignTasks = true
        )
        val caseSettingsDto = CaseSettingsDto(
            canHaveAssignee = false
        )
        val updatedCaseSettings = caseSettingsDto.update(currentCaseSettings)

        assertTrue(currentCaseSettings.canHaveAssignee)
        assertTrue(currentCaseSettings.autoAssignTasks)
        assertNull(caseSettingsDto.autoAssignTasks)
        assertFalse(updatedCaseSettings.canHaveAssignee)
        assertFalse(updatedCaseSettings.canHaveAssignee)
    }
}