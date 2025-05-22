/*
 *  Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.valtimo.formflow.web.rest.result

import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.domain.definition.configuration.FormFlowStep

data class FormFlowDefinitionDto(
    val key: String,
    val version: Long,
    val startStep: String,
    val steps: List<FormFlowStep>,
    val readOnly: Boolean = false
) {
    fun toEntity(): FormFlowDefinition = FormFlowDefinition(
        id = FormFlowDefinitionId(key, version),
        startStep = startStep,
        steps = steps.map { it.toDefinition() }.toSet()
    )

    companion object {
        fun of(formFlowDefinition: FormFlowDefinition, readOnly: Boolean): FormFlowDefinitionDto =
            FormFlowDefinitionDto(
                key = formFlowDefinition.id.key,
                version = formFlowDefinition.id.version,
                startStep = formFlowDefinition.startStep,
                steps = formFlowDefinition.getOrderedSteps().map { FormFlowStep.fromEntity(it) },
                readOnly = readOnly
            )
    }
}
