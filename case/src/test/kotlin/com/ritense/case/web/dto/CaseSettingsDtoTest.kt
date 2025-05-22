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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse

class CaseSettingsDtoTest: BaseTest() {
    @Test
    fun `should update case settings when value is not null`() {
        val currentCaseWithSettings = caseDefinition()
        assertThat(currentCaseWithSettings.canHaveAssignee).isFalse()

        val caseSettingsDto = CaseSettingsDto(
            canHaveAssignee = true
        )
        assertThat(caseSettingsDto.canHaveAssignee!!).isTrue()

        val updatedCaseSettings = caseSettingsDto.update(currentCaseWithSettings)
        assertThat(updatedCaseSettings.canHaveAssignee).isTrue()
    }

    @Test
    fun `should update case setting 'hasExternalStartForm' when url value is not null`() {
        val externalFormUrl = "https://example.com/create-case-form"
        val currentCaseWithSettings = caseDefinition()

        assertThat(currentCaseWithSettings.hasExternalStartForm).isFalse()
        assertThat(currentCaseWithSettings.externalStartFormUrl).isNull()

        val caseSettingsDto = CaseSettingsDto(
            hasExternalStartForm = true,
            externalStartFormUrl = externalFormUrl
        )
        val updatedCaseSettings = caseSettingsDto.update(currentCaseWithSettings)

        assertThat(updatedCaseSettings.hasExternalStartForm).isTrue()
        assertThat(updatedCaseSettings.externalStartFormUrl).isEqualTo(externalFormUrl)
    }

    @Test
    fun `should throw IllegalArgumentException when updating case setting 'hasExternalStartForm' and url value is blank`() {
        val currentCaseWithSettings = caseDefinition()
        assertThat(currentCaseWithSettings.hasExternalStartForm).isFalse()
        assertThat(currentCaseWithSettings.externalStartFormUrl).isNull()

        val caseSettingsDto = CaseSettingsDto(
            hasExternalStartForm = true,
            externalStartFormUrl = "   "
        )
        assertThat(caseSettingsDto.hasExternalStartForm).isTrue()
        assertThat(caseSettingsDto.externalStartFormUrl).isBlank()

        assertThrows<IllegalArgumentException> {
            caseSettingsDto.update(currentCaseWithSettings)
        }.let { exception ->
            assertThat(exception.message)
                .isEqualTo("Case property [hasExternalStartForm] can only be true when [externalStartFormUrl] is not null or blank.")
        }
    }

    @Test
    fun `should throw IllegalArgumentException when updating case setting 'hasExternalStartForm' is not a valid url`() {
        val currentCaseWithSettings = caseDefinition()
        assertThat(currentCaseWithSettings.hasExternalStartForm).isFalse()
        assertThat(currentCaseWithSettings.externalStartFormUrl).isNull()

        val caseSettingsDto = CaseSettingsDto(
            hasExternalStartForm = true,
            externalStartFormUrl = "this is not a valid url"
        )
        assertThat(caseSettingsDto.hasExternalStartForm).isTrue()
        assertThat(caseSettingsDto.externalStartFormUrl).isNotBlank()

        assertThrows<IllegalArgumentException> {
            caseSettingsDto.update(currentCaseWithSettings)
        }.let { exception ->
            assertThat(exception.message)
                .isEqualTo("Case property [externalStartFormUrl] is not a valid URL.")
        }
    }

    @Test
    fun `should throw IllegalArgumentException when updating case setting 'hasExternalStartForm' exceeds 512 characters`() {
        val currentCaseWithSettings = caseDefinition()
        assertThat(currentCaseWithSettings.hasExternalStartForm).isFalse()
        assertThat(currentCaseWithSettings.externalStartFormUrl).isNull()

        val caseSettingsDto = CaseSettingsDto(
            hasExternalStartForm = true,
            externalStartFormUrl = "https://www.example.com/search?param1=value10&param2=value20&param3=value30&param4=value40&param5=value50&param6=value60&param7=value70&param8=value80&param9=value90&param10=value100&param11=value110&param12=value120&param13=value130&param14=value140&param15=value150&param16=value160&param17=value170&param18=value180&param19=value190&param20=value200&param21=value210&param22=value220&param23=value230&param24=value240&param25=value250&param26=value260&param27=value270&param28=value280&param29=value290&extra_param=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
        )
        assertThat(caseSettingsDto.hasExternalStartForm).isTrue()
        assertThat(caseSettingsDto.externalStartFormUrl).isNotBlank()

        assertThrows<IllegalArgumentException> {
            caseSettingsDto.update(currentCaseWithSettings)
        }.let { exception ->
            assertThat(exception.message)
                .isEqualTo("Case property [externalStartFormUrl] exceeds the maximum length of 512 characters.")
        }
    }

    @Test
    fun `should not update case settings when value is null`() {
        val currentCaseWithSettings = caseDefinition()
        assertFalse(currentCaseWithSettings.canHaveAssignee)
        val caseSettingsDto = CaseSettingsDto()
        assertThat(caseSettingsDto.canHaveAssignee).isNull()
        assertThat(caseSettingsDto.hasExternalStartForm).isNull()

        val updatedCaseSettings = caseSettingsDto.update(currentCaseWithSettings)
        assertThat(updatedCaseSettings.canHaveAssignee).isFalse()
        assertThat(updatedCaseSettings.hasExternalStartForm).isFalse()
    }

    @Test
    fun `should set autoAssignTasks to false when canHaveAssignee is set to false`() {
        val currentCaseWithSettings = caseDefinition(
            canHaveAssignee = true,
            autoAssignTasks = true
        )
        assertThat(currentCaseWithSettings.autoAssignTasks).isTrue()
        assertThat(currentCaseWithSettings.canHaveAssignee).isTrue()

        val caseSettingsDto = CaseSettingsDto(
            canHaveAssignee = false
        )
        assertThat(caseSettingsDto.autoAssignTasks).isNull()

        val updatedCaseSettings = caseSettingsDto.update(currentCaseWithSettings)
        assertThat(updatedCaseSettings.canHaveAssignee).isFalse()
        assertThat(updatedCaseSettings.autoAssignTasks).isFalse()
    }
}