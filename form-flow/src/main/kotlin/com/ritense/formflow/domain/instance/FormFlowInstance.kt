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

import com.ritense.formflow.domain.definition.FormFlowDefinition
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.AttributeOverride
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinColumns
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import mu.withLoggingContext
import java.util.Objects
import org.hibernate.annotations.Type
import org.json.JSONObject


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
    @Type(value = JsonType::class)
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
        return withLoggingContext(FormFlowStepInstance::class.java.canonicalName to currentFormFlowStepInstanceId.toString()) {
            if (this.currentFormFlowStepInstanceId != currentFormFlowStepInstanceId) {
                return getCurrentStep()
            }

            val formFlowStepInstance = getCurrentStep()

            formFlowStepInstance.complete(submissionData.toString())

            val nextStep = navigateToNextStep()
            check(nextStep != null || formFlowStepInstance.definition.onComplete.isNotEmpty()) {
                "Form flow end reached but no action was taken because the 'onComplete' is empty. For form flow step: '${formFlowStepInstance.definition.id}'"
            }
            nextStep
        }
    }

    /**
     * This method navigates to the previous step (if present).
     *
     * @return The previous step (optional)
     */
    fun back(): FormFlowStepInstance? {
        val currentStep = getCurrentStep()
        val previousStepOrder = currentStep.order - 1
        return if (previousStepOrder >= 0) {
            currentStep.back()
            val previousStep = history.single { it.order == previousStepOrder }
            currentFormFlowStepInstanceId = previousStep.id
            previousStep
        } else {
            null
        }
    }

    /**
     * This method navigates to the target step.
     *
     * @return The target step
     */
    fun navigateToStep(targetId: FormFlowStepInstanceId): FormFlowStepInstance {
        return withLoggingContext(FormFlowStepInstance::class.java.canonicalName to targetId.toString()) {
            val targetStep = history.single { it.id == targetId }
            val currentStep = getCurrentStep()
            if (targetStep.order < currentStep.order) {
                for (i in history.indexOf(currentStep) downTo history.indexOf(targetStep) + 1) {
                    history[i].back()
                }
            }
            currentFormFlowStepInstanceId = targetStep.id
            targetStep
        }
    }

    /**
     * This method saves submission data for the current step but will *not* complete the step.
     * The submitted data can be changed at any time.
     * @param incompleteSubmissionData This data will be set as the submissionData of the step.
     *
     * @return The previous step (optional)
     */
    fun saveTemporary(incompleteSubmissionData: JSONObject) {
        getCurrentStep().saveTemporary(incompleteSubmissionData.toString())
    }

    fun getCurrentStep(): FormFlowStepInstance {
        requireNotNull(currentFormFlowStepInstanceId) {
            "Failed to get current step. Form flow '${formFlowDefinition.id.key}' has ended."
        }
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
        val currentStepOrder = if (currentFormFlowStepInstanceId == null) {
            Int.MAX_VALUE
        } else {
            getCurrentStep().order
        }
        return history
            .filter { it.order <= currentStepOrder }
            .sortedBy { it.submissionOrder }
            .mapNotNull { it.getCurrentSubmissionData() }
            .map { JSONObject(it) }
            .toList()
    }

    private fun mergeSubmissionData(source: JSONObject, target: JSONObject) {
        val keys = JSONObject.getNames(source) ?: arrayOf()
        for (key in keys) {
            val value = source[key]
            if (target.has(key) && value is JSONObject) {
                mergeSubmissionData(value, target.getJSONObject(key))
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

        val formFlowStepInstance = history.firstOrNull {
            it.order == nextStep.order && it.stepKey == nextStep.stepKey
        }

        if (formFlowStepInstance == null) {
            history.removeIf { (it.order >= nextStep.order)}
            history.add(nextStep.order, nextStep)
        }

        currentFormFlowStepInstanceId = nextStep.id
        return nextStep
    }

    private fun determineNextStep() : FormFlowStepInstance? {
        if (currentFormFlowStepInstanceId == null && history.isNotEmpty()) {
            return null
        }

        val stepKey: String
        val stepOrder: Int
        if (currentFormFlowStepInstanceId == null) {
            stepKey = formFlowDefinition.startStep
            stepOrder = 0
        } else  {
            val currentStepInstance = getCurrentStep()
            val nextStep = currentStepInstance.determineNextStep() ?: return null

            stepKey = nextStep.step
            stepOrder = currentStepInstance.order + 1
        }
        return history.singleOrNull { it.stepKey == stepKey && it.order == stepOrder }
            ?: FormFlowStepInstance(instance = this, stepKey = stepKey, order = stepOrder)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FormFlowInstance

        if (id != other.id) return false
        if (formFlowDefinition.id != other.formFlowDefinition.id) return false
        if (currentFormFlowStepInstanceId != other.currentFormFlowStepInstanceId) return false
        // Wrapping in ArrayList to prevent issues with Hibernate PersistentBag equals implementation
        // See https://hibernate.atlassian.net/browse/HHH-5409 for more details
        if (ArrayList(history) != ArrayList(other.history)) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(id, formFlowDefinition.id, currentFormFlowStepInstanceId, history, additionalProperties)
    }

}
