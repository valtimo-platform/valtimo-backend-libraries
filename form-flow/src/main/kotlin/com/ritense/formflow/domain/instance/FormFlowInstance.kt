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

import com.ritense.formflow.domain.definition.FormFlowDefinition
import org.hibernate.annotations.Type
import org.json.JSONObject
import java.util.Objects
import javax.persistence.AttributeOverride
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OrderBy
import javax.persistence.Table


@Entity
@Table(name = "form_flow_instance")
class FormFlowInstance(
    @EmbeddedId
    val id: FormFlowInstanceId = FormFlowInstanceId.newId(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns(
        JoinColumn(name = "form_flow_definition_key", referencedColumnName = "form_flow_definition_key"),
        JoinColumn(name = "form_flow_definition_version", referencedColumnName = "form_flow_definition_version")
    )
    val formFlowDefinition: FormFlowDefinition,
    @Embedded
    @AttributeOverride(name = "id", column = Column(name = "current_form_flow_step_instance_id"))
    var currentFormFlowStepInstanceId: FormFlowStepInstanceId? = null,
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "instance")
    @OrderBy("order ASC")
    private val history: MutableList<FormFlowStepInstance> = mutableListOf(),
    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(name = "additional_properties", columnDefinition = "json", nullable = false)
    private val additionalProperties: MutableMap<String, Any> = mutableMapOf()
) {
    init {
        navigateToNextStep()
    }

    /**
     * This method completes the current step, and navigates to the next (if present).
     * The next step will not be opened by this method.
     *
     * @param currentFormFlowStepInstanceId The intended step to complete
     * @param submissionData This data will be set as the submissionData of the step
     * @return The next step (optional)
     */
    fun complete(
        currentFormFlowStepInstanceId: FormFlowStepInstanceId,
        submissionData: JSONObject
    ): FormFlowStepInstance? {
        assert(this.currentFormFlowStepInstanceId == currentFormFlowStepInstanceId)

        val formFlowStepInstance = getCurrentStep()

        formFlowStepInstance.complete(submissionData.toString())

        return navigateToNextStep()
    }

    fun getCurrentStep(): FormFlowStepInstance {
        return history.first {
            it.id == currentFormFlowStepInstanceId
        }
    }

    fun getHistory() : List<FormFlowStepInstance> {
        return history
    }

    fun getAdditionalProperties() : Map<String, Any> {
        return additionalProperties
    }

    fun getSubmissionDataContext(): String {
        val formFlowStepInstancesSubmissionData = getSubmissionData()

        if (formFlowStepInstancesSubmissionData.isEmpty()) {
            return JSONObject().toString()
        }

        val mergedSubmissionData = formFlowStepInstancesSubmissionData.first()

        if (formFlowStepInstancesSubmissionData.size == 1) {
            return mergedSubmissionData.toString()
        }

        formFlowStepInstancesSubmissionData.subList(1, formFlowStepInstancesSubmissionData.size).forEach {
            mergeSubmissionData(it, mergedSubmissionData)
        }

        return mergedSubmissionData.toString()
    }

    private fun getSubmissionData() : List<JSONObject> {
        val currentStepOrder = getCurrentStep().order
        return history.filter {
            it.order <= currentStepOrder && it.submissionData != null
        }.map {
            JSONObject(it.submissionData)
        }
    }

    private fun mergeSubmissionData(source: JSONObject, target: JSONObject) {
        for (key in JSONObject.getNames(source)) {
            val value = source.get(key)
            if (target.has(key) && value is JSONObject) {
                mergeSubmissionData(value as JSONObject, target.getJSONObject(key))
            } else {
                // Assumption: Anything that isn't a JSONObject can be overwritten
                target.put(key, value)
            }
        }
    }

    private fun navigateToNextStep() : FormFlowStepInstance? {
        val nextStep = determineNextStep()
        if (nextStep == null) {
            this.currentFormFlowStepInstanceId = null
            return null
        }
        history.add(nextStep)
        currentFormFlowStepInstanceId = nextStep.id
        return nextStep
    }

    private fun determineNextStep() : FormFlowStepInstance? {
        if (currentFormFlowStepInstanceId == null && history.isNotEmpty()) {
            return null
        }
        var stepKey = formFlowDefinition.startStep
        if (currentFormFlowStepInstanceId != null) {
            val currentStepInstance = getCurrentStep()
            val currentStep = formFlowDefinition.steps.first {
                it.id.key == currentStepInstance.stepKey
            }

            if (currentStep.nextSteps == null || currentStep.nextSteps.isEmpty()) {
                return null
            }

            stepKey = currentStep.nextSteps.first().step
        }
        return FormFlowStepInstance(instance = this, stepKey = stepKey, order = history.size)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FormFlowInstance

        if (id != other.id) return false
        if (formFlowDefinition.id != other.formFlowDefinition.id) return false
        if (currentFormFlowStepInstanceId != other.currentFormFlowStepInstanceId) return false
        //wrapping in arraylist to prevent issues with hibernate PersistentBag equals implementation
        if (ArrayList(history) != ArrayList(other.history)) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(id, formFlowDefinition.id, currentFormFlowStepInstanceId, history, additionalProperties)
    }

}