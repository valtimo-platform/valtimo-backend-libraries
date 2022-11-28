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

package com.ritense.case.web.rest.dto

import com.ritense.case.domain.CaseDefinitionSettings
import java.util.Optional

class CaseSettingsDto(
    val canHaveAssignee: Optional<Boolean>?
) {
    fun update(currentSettings: CaseDefinitionSettings): CaseDefinitionSettings{
        return CaseDefinitionSettings(
            currentSettings.name,
            getSettingForUpdate(currentSettings.canHaveAssignee, this.canHaveAssignee)?: false
        )
    }

    private fun <T> getSettingForUpdate(currentValue: T?, newValue: Optional<T>?): T? {
        return if (newValue != null) {
            newValue.orElse(null)
        } else {
            currentValue
        }
    }
}