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

package com.ritense.formflow.domain.definition.configuration

import com.ritense.formflow.domain.definition.FormFlowDefinitionId

class FormFlowDefinition(
    val startStep: String,

    val steps: Set<FormFlowStep>
) {
    fun contentEquals(other: com.ritense.formflow.domain.definition.FormFlowDefinition): Boolean {
        if (startStep != other.startStep) return false
        if (steps.size != other.steps.size) return false

        for (otherStep in other.steps) {
            var hasMatch = false
            for (step in steps) {
                if (step.contentEquals(otherStep)) {
                    hasMatch = true
                    break
                }
            }
            if (!hasMatch) return false
        }

        if (steps != other.steps) return false

        return true
    }

    fun toDefinition(id: FormFlowDefinitionId) : com.ritense.formflow.domain.definition.FormFlowDefinition {
        val definitionSteps: Set<com.ritense.formflow.domain.definition.FormFlowStep> = steps.map {
            it.toDefinition()
        }.toSet()

        return com.ritense.formflow.domain.definition.FormFlowDefinition(id, startStep, definitionSteps)
    }

}