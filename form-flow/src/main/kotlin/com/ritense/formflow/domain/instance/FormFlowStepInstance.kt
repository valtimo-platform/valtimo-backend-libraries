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

package com.ritense.formflow.domain.instance

import com.ritense.formflow.domain.definition.FormFlowStep
import com.ritense.formflow.expression.ExpressionProcessorFactoryHolder
import org.hibernate.annotations.Type
import java.util.Objects
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "form_flow_step_instance")
data class FormFlowStepInstance(
    @EmbeddedId
    val id: FormFlowStepInstanceId = FormFlowStepInstanceId.newId(),
    @JoinColumn(name = "form_flow_instance_id", updatable = false, nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    val instance: FormFlowInstance,
    @Column(name = "form_flow_step_key", updatable = false, nullable = false)
    val stepKey: String,
    @Column(name = "form_flow_step_instance_order", updatable = false, nullable = false)
    val order: Int,
    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(name = "submission_data")
    var submissionData: String? = null
) {

    val definition: FormFlowStep
        get() = instance.formFlowDefinition.getStepByKey(stepKey)

    fun back() {
        processExpressions(FormFlowStep::onBack)
    }
    fun open() {
        processExpressions(FormFlowStep::onOpen)
    }

    fun complete(submissionData: String) {
        this.submissionData = submissionData

        processExpressions(FormFlowStep::onComplete)
    }

    private fun processExpressions(expressionist: (FormFlowStep)-> List<String>?) {
        ExpressionProcessorFactoryHolder.getinstance()?.let {
            val variables = createVarMap()
            val expressionProcessor = it.create(variables)

            expressionist(definition)?.forEach { expression ->
                expressionProcessor.process<Any>(expression)
            }
        }
    }

    private fun createVarMap(): Map<String, Any> {
        return mapOf(
            "step" to mapOf(
                "id" to id,
                "key" to stepKey,
                "submissionData" to instance.getSubmissionDataContext()
            ),
            "instance" to mapOf(
                "id" to instance.id
            ),
            "additionalProperties" to instance.getAdditionalProperties()
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FormFlowStepInstance

        if (id != other.id) return false
        if (stepKey != other.stepKey) return false
        if (order != other.order) return false
        if (submissionData != other.submissionData) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(id, stepKey, order, submissionData)
    }
}
