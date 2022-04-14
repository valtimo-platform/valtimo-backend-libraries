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

package com.ritense.formflow.domain.definition

import com.ritense.formflow.domain.instance.FormFlowInstance
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "form_flow_definition")
class FormFlowDefinition(

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

    fun createInstance(additionalProperties: MutableMap<String, Any>) : FormFlowInstance {
        return FormFlowInstance(formFlowDefinition = this,
            additionalProperties = additionalProperties)
    }

    fun getStepByKey(key: String): FormFlowStep {
        return steps.first { it.id.key == key }
    }

}