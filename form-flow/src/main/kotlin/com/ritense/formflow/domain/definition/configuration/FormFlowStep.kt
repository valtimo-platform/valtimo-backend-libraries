/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

import com.ritense.formflow.domain.definition.FormFlowStepId
import com.ritense.formflow.domain.definition.FormFlowStep as FormFlowStepEntity

data class FormFlowStep(
    val key: String,
    val nextStep: String? = null,
    val nextSteps: List<FormFlowNextStep> = listOf(),
    val onBack: List<String> = listOf(),
    val onOpen: List<String> = listOf(),
    val onComplete: List<String> = listOf(),
    val type: FormFlowStepType
) {

    fun contentEquals(other: FormFlowStepEntity): Boolean {
        if (key != other.id.key) return false

        if (nextSteps.size != other.nextSteps.size) return false
        if (nextSteps.any { nextStep ->
            other.nextSteps.none { otherNextStep ->
                nextStep.contentEquals(otherNextStep)
            }
        }) return false

        if (onBack != other.onBack.toList()) return false
        if (onOpen != other.onOpen.toList()) return false
        if (onComplete != other.onComplete.toList()) return false
        if (type != other.type) return false

        return true
    }

    fun toDefinition(): FormFlowStepEntity {
        val nextSteps =
            if (this.nextStep != null)
                listOf(FormFlowNextStep(step = this.nextStep).toDefinition())
            else
                this.nextSteps.map(FormFlowNextStep::toDefinition)


        return FormFlowStepEntity(
            FormFlowStepId.create(key),
            nextSteps,
            onBack,
            onOpen,
            onComplete,
            type
        )
    }
}
