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

package com.ritense.formflow.domain

import com.ritense.formflow.SpringContextHelper
import com.ritense.formflow.repository.FormFlowInstanceRepository
import javax.persistence.AttributeOverride
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "form_flow_instance")
class FormFlowInstance(
    @EmbeddedId
    val id: FormFlowInstanceId = FormFlowInstanceId.newId(),
    @Embedded
    @AttributeOverride(name = "key", column = Column(name = "form_flow_definition_key"))
    @AttributeOverride(name = "version", column = Column(name = "form_flow_definition_version"))
    val formFlowDefinitionId: FormFlowDefinitionId,
    additionalProperties: Map<String, Any> = emptyMap(),
    @Embedded
    val context: FormFlowInstanceContext =
        FormFlowInstanceContext(additionalProperties = additionalProperties.toMutableMap()),
    @Embedded
    @AttributeOverride(name = "id", column = Column(name = "current_form_flow_step_instance_id"))
    var currentFormFlowStepInstanceId: FormFlowStepInstanceId? = null
) {
    init {
        navigateToNextStep()
    }

    fun complete(
        currentFormFlowStepInstanceId: FormFlowStepInstanceId,
        submissionData: String
    ) : FormFlowStepInstanceId {
        assert(this.currentFormFlowStepInstanceId == currentFormFlowStepInstanceId)

        val formFlowStepInstance = context.getHistory()
            .first { formFlowStepInstance -> formFlowStepInstance.id == currentFormFlowStepInstanceId }

        formFlowStepInstance.submissionData = submissionData

        val nextStep = navigateToNextStep()
        save()
        return nextStep.id
    }

    fun save() : FormFlowInstance {
        return SpringContextHelper.getBean(FormFlowInstanceRepository::class.java).save(this)
    }

    private fun navigateToNextStep() : FormFlowStepInstance {
        val nextStep = determineNextStep()
        context.addStep(nextStep)
        currentFormFlowStepInstanceId = nextStep.id
        return nextStep
    }

    private fun determineNextStep() : FormFlowStepInstance {
        return FormFlowStepInstance(instance = this, stepKey = "henk", order = context.getHistory().size)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FormFlowInstance

        if (id != other.id) return false
        if (formFlowDefinitionId != other.formFlowDefinitionId) return false
        if (context != other.context) return false
        if (currentFormFlowStepInstanceId != other.currentFormFlowStepInstanceId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + formFlowDefinitionId.hashCode()
        result = 31 * result + context.hashCode()
        result = 31 * result + (currentFormFlowStepInstanceId?.hashCode() ?: 0)
        return result
    }


}