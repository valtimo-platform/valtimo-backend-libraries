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

package com.ritense.formflow.domain.definition.configuration

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.domain.definition.FormFlowDefinition as FormFlowDefinitionEntity
import com.ritense.formflow.domain.definition.FormFlowStep as FormFlowStepEntity

@JsonInclude(Include.NON_EMPTY)
data class FormFlowDefinition(
    val startStep: String,
    val steps: Set<FormFlowStep>
) {
    fun contentEquals(other: FormFlowDefinitionEntity): Boolean {
        if (startStep != other.startStep) return false

        if (steps.size != other.steps.size) return false
        if(steps.any {step ->
            other.steps.none { otherStep ->
                step.contentEquals(otherStep)
            }
        }) return false

        return true
    }

    fun toDefinition(id: FormFlowDefinitionId) : FormFlowDefinitionEntity {
        val definitionSteps: Set<FormFlowStepEntity> = steps
            .map(FormFlowStep::toDefinition)
            .toSet()

        return FormFlowDefinitionEntity(id, startStep, definitionSteps)
    }

    companion object {
        fun fromEntity(entity: FormFlowDefinitionEntity): FormFlowDefinition {
            return FormFlowDefinition(
                startStep = entity.startStep,
                steps = entity.steps.map {
                    FormFlowStep.fromEntity(it)
                }.toSet()
            )
        }
    }

}