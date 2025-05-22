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

package com.ritense.case.web.rest.dto

import com.ritense.case_.domain.definition.CaseDefinition

data class CaseSettingsDto(
    val canHaveAssignee: Boolean? = null,
    val autoAssignTasks: Boolean? = null,
    val hasExternalStartForm: Boolean? = null,
    val externalStartFormUrl: String? = null,
    val externalStartFormDescription: String? = null,
) {
    fun update(currentCaseDefinition: CaseDefinition): CaseDefinition {
        return currentCaseDefinition.copy(
            name = currentCaseDefinition.name,
            canHaveAssignee = getSettingForUpdate(currentCaseDefinition.canHaveAssignee, this.canHaveAssignee) ?: false,
            autoAssignTasks = when (this.canHaveAssignee) {
                false -> false
                else -> getSettingForUpdate(currentCaseDefinition.autoAssignTasks, this.autoAssignTasks) ?: false
            },
            hasExternalStartForm = getSettingForUpdate(
                currentCaseDefinition.hasExternalStartForm,
                this.hasExternalStartForm
            ) ?: false,
            externalStartFormUrl = when (this.hasExternalStartForm) {
                false -> null
                else -> getSettingForUpdate(currentCaseDefinition.externalStartFormUrl, this.externalStartFormUrl)
            },
            externalStartFormDescription = when (this.hasExternalStartForm) {
                false -> null
                else -> getSettingForUpdate(
                    currentCaseDefinition.externalStartFormUrl,
                    this.externalStartFormDescription
                )
            }
        )
    }

    private fun <T> getSettingForUpdate(currentValue: T, newValue: T?): T {
        return newValue ?: currentValue
    }

    companion object {

        fun from(caseDefinition: CaseDefinition) = CaseSettingsDto(
            canHaveAssignee = caseDefinition.canHaveAssignee,
            autoAssignTasks = caseDefinition.autoAssignTasks,
            hasExternalStartForm = caseDefinition.hasExternalStartForm,
            externalStartFormUrl = caseDefinition.externalStartFormUrl,
            externalStartFormDescription = caseDefinition.externalStartFormDescription,
        )
    }
}