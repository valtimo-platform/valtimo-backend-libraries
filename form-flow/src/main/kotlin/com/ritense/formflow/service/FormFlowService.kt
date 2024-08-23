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

package com.ritense.formflow.service

import com.ritense.formflow.domain.FormFlowBreadcrumb
import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.domain.definition.FormFlowStep
import com.ritense.formflow.domain.definition.configuration.FormFlowStepType
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.domain.instance.FormFlowInstanceId
import com.ritense.formflow.domain.instance.FormFlowStepInstance
import com.ritense.formflow.handler.FormFlowStepTypeHandler
import com.ritense.formflow.handler.TypeProperties
import com.ritense.formflow.repository.FormFlowAdditionalPropertiesSearchRepository
import com.ritense.formflow.repository.FormFlowDefinitionRepository
import com.ritense.formflow.repository.FormFlowInstanceRepository
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Transactional
class FormFlowService(
    private val formFlowDefinitionRepository: FormFlowDefinitionRepository,
    private val formFlowInstanceRepository: FormFlowInstanceRepository,
    private val formFlowAdditionalPropertiesSearchRepository: FormFlowAdditionalPropertiesSearchRepository,
    private val formFlowStepTypeHandlers: List<FormFlowStepTypeHandler>
) {

    fun getFormFlowDefinitions(): List<FormFlowDefinition> {
        return formFlowDefinitionRepository.findAll()
    }

    fun findDefinition(formFlowId: FormFlowDefinitionId): FormFlowDefinition {
        return formFlowDefinitionRepository.getReferenceById(formFlowId)
    }

    fun findDefinition(formFlowDefinitionId: String): FormFlowDefinition? {
        val formFlowIdAsArray = formFlowDefinitionId.split(":")
        if (formFlowIdAsArray.size != 2) {
            throw IllegalArgumentException("Invalid Format found for formFlowId '${formFlowIdAsArray.joinToString(":")}'. Form flow id must have format key:version")
        }
        return if (formFlowIdAsArray[1] == "latest") {
            findLatestDefinitionByKey(formFlowIdAsArray[0])
        } else {
            formFlowDefinitionRepository.findById(
                FormFlowDefinitionId(formFlowIdAsArray[0], formFlowIdAsArray[1].toLong())
            ).getOrNull()
        }
    }

    fun findLatestDefinitionByKey(formFlowKey: String): FormFlowDefinition? {
        return formFlowDefinitionRepository.findFirstByIdKeyOrderByIdVersionDesc(formFlowKey)
    }

    fun save(formFlowDefinition: FormFlowDefinition): FormFlowDefinition {
        if (formFlowDefinitionRepository.existsById(formFlowDefinition.id)) {
            throw UnsupportedOperationException("Failed to save From Flow. Form Flow already exists: ${formFlowDefinition.id}")
        } else {
            return formFlowDefinitionRepository.save(formFlowDefinition)
        }
    }

    fun getInstanceById(formFlowInstanceId: FormFlowInstanceId): FormFlowInstance {
        return formFlowInstanceRepository.getReferenceById(formFlowInstanceId)
    }

    fun getByInstanceIdIfExists(formFlowInstanceId: FormFlowInstanceId): FormFlowInstance? {
        return formFlowInstanceRepository.getReferenceById(formFlowInstanceId)
    }

    fun save(formFlowInstance: FormFlowInstance): FormFlowInstance {
        return formFlowInstanceRepository.save(formFlowInstance)
    }

    fun findInstances(additionalProperties: Map<String, Any>): List<FormFlowInstance> {
        return formFlowAdditionalPropertiesSearchRepository.findInstances(additionalProperties)
    }

    fun getFormFlowStepTypeHandler(stepType: FormFlowStepType): FormFlowStepTypeHandler {
        return formFlowStepTypeHandlers.singleOrNull { it.getType() == stepType.name }
            ?: throw IllegalStateException("No formFlowStepTypeHandler found for type '${stepType.name}'")
    }

    fun getTypeProperties(stepInstance: FormFlowStepInstance): TypeProperties {
        return getFormFlowStepTypeHandler(stepInstance.definition.type).getTypeProperties(stepInstance)
    }

    fun deleteByKey(definitionKey: String) {
        formFlowDefinitionRepository.deleteAllByIdKey(definitionKey)
    }

    fun getBreadcrumbs(instance: FormFlowInstance): List<FormFlowBreadcrumb> {
        val lastCompletedOrder = instance.getHistory()
            .filter { it.submissionData != null }
            .maxByOrNull { it.submissionOrder }
            ?.order ?: -1
        val historicBreadcrumbs = instance.getHistory()
            .map { FormFlowBreadcrumb.of(it, it.order <= lastCompletedOrder + 1, it.order <= lastCompletedOrder) }
        val futureBreadcrumbs = getFutureSteps(instance)
            .map { FormFlowBreadcrumb.of(it) }
        return historicBreadcrumbs + futureBreadcrumbs
    }

    private fun getFutureSteps(instance: FormFlowInstance): List<FormFlowStep> {
        return getFutureSteps(instance.getHistory().last().definition)
    }

    private fun getFutureSteps(step: FormFlowStep, result: MutableList<FormFlowStep> = mutableListOf()): List<FormFlowStep> {
        step.nextSteps.forEach { nextStep ->
            val featureStep = step.id.formFlowDefinition!!.steps.first { it.id.key == nextStep.step }
            if (!result.contains(featureStep)) {
                result.add(featureStep)
                return getFutureSteps(featureStep, result)
            }
        }
        return result
    }
}
