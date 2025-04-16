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

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.ritense.case_.domain.definition.CaseDefinition
import java.time.LocalDateTime

@JsonInclude(Include.NON_EMPTY)
data class CaseDefinitionResponseDto(
    val caseDefinitionKey: String,
    val caseDefinitionVersionTag: String,
    val name: String,
    val description: String?,
    val createdBy: String?,
    val createdDate: LocalDateTime?,
    val basedOnVersionTag: String?,
    val final: Boolean,
    val active: Boolean,

    val canHaveAssignee: Boolean,
    val autoAssignTasks: Boolean,
    val hasExternalStartForm: Boolean? = null,
    val externalStartFormUrl: String? = null,
    val externalStartFormDescription: String? = null,

    val conflictingVersions: String? = null,
) {
    companion object {
        fun of(caseDefinition: CaseDefinition) =
            CaseDefinitionResponseDto(
                caseDefinitionKey =  caseDefinition.id.key,
                caseDefinitionVersionTag =  caseDefinition.id.versionTag.version,
                name =  caseDefinition.name,
                description =  caseDefinition.description,
                createdBy =  caseDefinition.createdBy,
                createdDate =  caseDefinition.createdDate,
                basedOnVersionTag =  caseDefinition.basedOnVersionTag?.version,
                final =  caseDefinition.final,
                active =  caseDefinition.active,

                canHaveAssignee =  caseDefinition.canHaveAssignee,
                autoAssignTasks =  caseDefinition.autoAssignTasks,
                hasExternalStartForm =  caseDefinition.hasExternalStartForm,
                externalStartFormUrl =  caseDefinition.externalStartFormUrl
            )

    fun of(caseDefinition: CaseDefinition, conflictingVersions: String?) =
        CaseDefinitionResponseDto(
            caseDefinitionKey = caseDefinition.id.key,
            caseDefinitionVersionTag = caseDefinition.id.versionTag.version,
            name = caseDefinition.name,
            description = caseDefinition.description,
            createdBy = caseDefinition.createdBy,
            createdDate = caseDefinition.createdDate,
            basedOnVersionTag = caseDefinition.basedOnVersionTag?.version,
            final = caseDefinition.final,
            active = caseDefinition.active,

            canHaveAssignee = caseDefinition.canHaveAssignee,
            autoAssignTasks = caseDefinition.autoAssignTasks,
            hasExternalStartForm = caseDefinition.hasExternalStartForm,
            externalStartFormUrl = caseDefinition.externalStartFormUrl,

            conflictingVersions = conflictingVersions,
        )
    }
}