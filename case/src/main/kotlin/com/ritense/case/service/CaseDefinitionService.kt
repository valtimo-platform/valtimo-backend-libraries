/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.case.service

import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.repository.CaseDefinitionSettingsRepository
import com.ritense.case.web.rest.dto.CaseSettingsDto

class CaseDefinitionService(
    private val repository: CaseDefinitionSettingsRepository
) {
    fun getCaseSettings(caseDefinitionName: String): CaseDefinitionSettings {
        return repository.getById(caseDefinitionName)
    }

    fun updateCaseSettings(caseDefinitionName: String, newSettings: CaseSettingsDto): CaseDefinitionSettings {
        val caseDefinitionSettings = repository.getById(caseDefinitionName)
        val updatedCaseDefinition = newSettings.update(caseDefinitionSettings)
        return repository.save(updatedCaseDefinition)
    }
}