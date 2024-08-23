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

package com.ritense.formflow.domain

import com.ritense.formflow.domain.definition.FormFlowStep
import com.ritense.formflow.domain.instance.FormFlowStepInstance
import java.util.UUID

data class FormFlowBreadcrumb(
    val title: String?,
    val key: String,
    val stepInstanceId: UUID? = null,
    val completed: Boolean,
) {
    companion object {

        fun of(step: FormFlowStepInstance, isNavigatable: Boolean = true, isCompleted: Boolean = true): FormFlowBreadcrumb {
            return FormFlowBreadcrumb(
                title = step.definition.title,
                key = step.definition.id.key,
                stepInstanceId = if (isNavigatable) step.id.id else null,
                completed = isCompleted,
            )
        }

        fun of(step: FormFlowStep): FormFlowBreadcrumb {
            return FormFlowBreadcrumb(
                title = step.title,
                key = step.id.key,
                completed = false,
            )
        }
    }
}