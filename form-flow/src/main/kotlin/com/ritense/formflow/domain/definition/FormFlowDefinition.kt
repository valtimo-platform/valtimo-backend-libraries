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

package com.ritense.formflow.domain.definition

import com.ritense.formflow.domain.instance.FormFlowInstance
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "form_flow_definition")
data class FormFlowDefinition(

    @EmbeddedId
    val id: FormFlowDefinitionId,

    @Column(name = "start_step")
    val startStep: String,

    @OneToMany(mappedBy = "id.formFlowDefinition", fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    val steps: Set<FormFlowStep>,
) {
    init {
        steps.forEach { step -> step.id.formFlowDefinition = this }
    }

    fun createInstance(additionalProperties: Map<String, Any>) : FormFlowInstance {
        return FormFlowInstance(formFlowDefinition = this,
            additionalProperties = additionalProperties.toMutableMap())
    }

    fun getStepByKey(key: String): FormFlowStep {
        return steps.first { it.id.key == key }
    }

}
