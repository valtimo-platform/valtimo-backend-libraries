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

class FormFlowStep(
    val key: String,
    val nextSteps: MutableList<FormFlowNextStep>? = ArrayList()
) {

    @JsonProperty("nextStep")
    fun nextStep(nextStep: String) {
        nextSteps!!.add(FormFlowNextStep(step = nextStep))
    }

    fun contentEquals(other: com.ritense.formflow.domain.definition.FormFlowStep): Boolean {
        if (key != other.id.key) return false
        if (nextSteps!!.size != other.nextSteps!!.size) return false
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

    fun toDefinition() : com.ritense.formflow.domain.definition.FormFlowStep {
        val nextSteps = this.nextSteps?.map{
            it.toDefinition()
        }?.toMutableList()
        return com.ritense.formflow.domain.definition.FormFlowStep(FormFlowStepId.create(key), nextSteps)
    }
}
