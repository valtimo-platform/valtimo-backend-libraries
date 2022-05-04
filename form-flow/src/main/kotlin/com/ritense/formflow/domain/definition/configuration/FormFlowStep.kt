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

import com.fasterxml.jackson.annotation.JsonProperty
import com.ritense.formflow.domain.definition.FormFlowStepId
import com.ritense.formflow.domain.definition.FormFlowStep as FormFlowStepEntity

class FormFlowStep(
    val key: String,
    val nextSteps: MutableList<FormFlowNextStep>? = ArrayList(),
    val onOpen: MutableList<String>? = ArrayList(),
    val onComplete: MutableList<String>? = ArrayList(),
    val type: FormFlowStepType
) {

    @JsonProperty("nextStep")
    fun nextStep(nextStep: String) {
        nextSteps!!.add(FormFlowNextStep(step = nextStep))
    }

    fun contentEquals(other: FormFlowStepEntity): Boolean {
        if (key != other.id.key) return false
        if (nextSteps!!.size != other.nextSteps!!.size) return false
        if (onOpen != other.onOpen) return false
        if (onComplete != other.onComplete) return false
        for (otherNextStep in other.nextSteps!!) {
            var hasMatch = false
            for (nextStep in nextSteps) {
                if (nextStep.contentEquals(otherNextStep)) {
                    hasMatch = true
                    break
                }
            }
            if (!hasMatch) return false
        }

        return true
    }

    fun toDefinition(): FormFlowStepEntity {
        val nextSteps = this.nextSteps?.map {
            it.toDefinition()
        }?.toMutableList()
        return FormFlowStepEntity(
            FormFlowStepId.create(key),
            nextSteps,
            onOpen ?: ArrayList(),
            onComplete ?: ArrayList(),
            type
        )
    }
}
