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

package com.ritense.formflow.domain.instance

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.formflow.domain.definition.FormFlowNextStep
import com.ritense.formflow.domain.definition.FormFlowStep
import com.ritense.formflow.event.ApplicationEventPublisherHolder
import com.ritense.formflow.event.FormFlowStepCompletedEvent
import com.ritense.formflow.expression.ExpressionProcessorFactoryHolder
import com.ritense.formflow.json.MapperSingleton
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import java.util.Objects

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
    @Column(name = "form_flow_step_instance_submission_order", updatable = false, nullable = false)
    var submissionOrder: Int,
    @Type(value = JsonType::class)
    @Column(name = "submission_data")
    var submissionData: String? = null,
    @Type(value = JsonType::class)
    @Column(name = "temporary_submission_data")
    var temporarySubmissionData: String? = null
    // On complete, clear temporary submission from the current step
    // We only use temporarySubmissionData of the current step when determining context
) {

    constructor(
        id: FormFlowStepInstanceId = FormFlowStepInstanceId.newId(),
        instance: FormFlowInstance,
        stepKey: String,
        order: Int,
        submissionData: String? = null,
        temporarySubmissionData: String? = null
    ) : this(id, instance, stepKey, order, nextSubmissionOrder(instance), submissionData, temporarySubmissionData)

    val definition: FormFlowStep
        get() = instance.formFlowDefinition.getStepByKey(stepKey)

    fun back() {
        processExpressions<Any>(definition.onBack)
    }

    fun saveTemporary(incompleteSubmissionData: String) {
        this.temporarySubmissionData = incompleteSubmissionData
    }

    fun open() {
        processExpressions<Any>(definition.onOpen)
    }

    fun complete(submissionData: String) {
        if (this.submissionData != submissionData) {
            this.submissionData = submissionData
            this.submissionOrder = nextSubmissionOrder(instance)
        }
        this.temporarySubmissionData = null

        processExpressions<Any>(definition.onComplete)
        ApplicationEventPublisherHolder.getInstance().publishEvent(
            FormFlowStepCompletedEvent(this)
        )
    }

    fun determineNextStep(): FormFlowNextStep? {
        val conditions = definition.nextSteps
            .map { nextStep -> nextStep.condition }

        val stepsWithResult = definition.nextSteps
            .zip(processExpressions<Boolean>(conditions))

        val firstStepWithResultTrue = stepsWithResult
            .firstOrNull { (_, result) -> result != null && result }
            ?.first

        if (firstStepWithResultTrue != null) {
            return firstStepWithResultTrue
        }

        return stepsWithResult
            .lastOrNull { (_, result) -> result == null }
            ?.first
    }

    private fun <T> processExpressions(expressions: List<String?>): List<T?> {
        return ExpressionProcessorFactoryHolder.getInstance().let { factory ->
            val variables = createVarMap()
            val expressionProcessor = factory.create(variables)

            val results = expressions.map { expression ->
                expression?.let { expressionProcessor.process<T>(expression) }
            }
            val newCompleteSubmissionData = MapperSingleton.get().valueToTree<JsonNode>(variables)
                .at("/step/submissionData")
            updateSubmissionData(newCompleteSubmissionData)
            results
        }
    }

    private fun updateSubmissionData(newSubmissionData: JsonNode) {
        val mapper = MapperSingleton.get()
        val oldCompleteSubmissionData = mapper.readValue<JsonNode>(instance.getSubmissionDataContext())
        if (newSubmissionData != oldCompleteSubmissionData) {
            keepDiff(newSubmissionData, oldCompleteSubmissionData)
            if (this.submissionData != null) {
                this.submissionData = newSubmissionData.toString()
            } else {
                this.temporarySubmissionData = newSubmissionData.toString()
            }
        }
    }

    private fun keepDiff(target: JsonNode, toRemove: JsonNode) {
        if (target is ObjectNode && toRemove is ObjectNode) {
            target.properties().toList().forEach { (key, value) ->
                val removeValue = toRemove.get(key)
                if (value == removeValue) {
                    target.remove(key)
                } else if (removeValue != null){
                    keepDiff(value, removeValue)
                }
            }
        }
    }

    private fun createVarMap(): Map<String, Any> {
        return mapOf(
            "step" to mapOf(
                "id" to id,
                "key" to stepKey,
                "submissionData" to MapperSingleton.get().readValue<JsonNode>(instance.getSubmissionDataContext())
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

    fun getCurrentSubmissionData(): String? {
        return temporarySubmissionData ?: submissionData
    }

    companion object {
        private fun nextSubmissionOrder(instance: FormFlowInstance): Int {
            return (instance.getHistory().maxOfOrNull { it.submissionOrder } ?: 0) + 1
        }
    }
}
