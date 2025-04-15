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

package com.ritense.case.web.dto

import com.ritense.BaseTest
import com.ritense.case.web.rest.dto.CaseSettingsDto
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CaseSettingsDtoTest: BaseTest() {
    @Test
    fun `should update case settings when value is not null`() {
        val currentCaseWithSettings = caseDefinition(CaseDefinitionId("key", "1.0.0"))
        val caseSettingsDto = CaseSettingsDto(canHaveAssignee = true)
        val updatedCaseSettings = caseSettingsDto.update(currentCaseWithSettings)

        assertFalse(currentCaseWithSettings.canHaveAssignee)
        assertTrue(caseSettingsDto.canHaveAssignee!!)
        assertTrue(updatedCaseSettings.canHaveAssignee)
    }

    @Test
    fun `should not update case settings when value is null`() {
        val currentCaseWithSettings = caseDefinition(CaseDefinitionId("key", "1.0.0"))
        val caseSettingsDto = CaseSettingsDto()
        val updatedCaseSettings = caseSettingsDto.update(currentCaseWithSettings)


        assertFalse(currentCaseWithSettings.canHaveAssignee)
        assertNull(caseSettingsDto.canHaveAssignee)
        assertFalse(updatedCaseSettings.canHaveAssignee)
    }
}