package com.ritense.case.web.dto

import com.ritense.case.web.rest.dto.CaseSettingsDto
import com.ritense.case_.domain.definition.CaseDefinition
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CaseSettingsDtoTest {
    @Test
    fun `should update case settings when value is not null`() {
        val currentCaseWithSettings = CaseDefinition(CaseDefinitionId("key", "1.0.0"), "name")
        val caseSettingsDto = CaseSettingsDto(canHaveAssignee = true)
        val updatedCaseSettings = caseSettingsDto.update(currentCaseWithSettings)

        assertFalse(currentCaseWithSettings.canHaveAssignee)
        assertTrue(caseSettingsDto.canHaveAssignee!!)
        assertTrue(updatedCaseSettings.canHaveAssignee)
    }

    @Test
    fun `should not update case settings when value is null`() {
        val currentCaseWithSettings = CaseDefinition(CaseDefinitionId("key", "1.0.0"), "name")
        val caseSettingsDto = CaseSettingsDto()
        val updatedCaseSettings = caseSettingsDto.update(currentCaseWithSettings)


        assertFalse(currentCaseWithSettings.canHaveAssignee)
        assertNull(caseSettingsDto.canHaveAssignee)
        assertFalse(updatedCaseSettings.canHaveAssignee)
    }
}