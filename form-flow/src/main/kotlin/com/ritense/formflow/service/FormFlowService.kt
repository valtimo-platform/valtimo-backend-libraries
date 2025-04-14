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
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import mu.withLoggingContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
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

    fun getFormFlowDefinitions(caseDefinitionId: CaseDefinitionId): List<FormFlowDefinition> {
        return formFlowDefinitionRepository.findAllByIdCaseDefinitionId(caseDefinitionId)
    }

    fun getFormFlowDefinitions(caseDefinitionId: CaseDefinitionId, pageable: Pageable): Page<FormFlowDefinition> {
        return formFlowDefinitionRepository.findAllByIdCaseDefinitionId(caseDefinitionId, pageable)
    }

    fun findDefinition(formFlowId: FormFlowDefinitionId): FormFlowDefinition {
        withLoggingContext(FormFlowDefinition::class.java.canonicalName to formFlowId.toString()) {
            return formFlowDefinitionRepository.getReferenceById(formFlowId)
        }
    }

    fun findDefinition(formFlowDefinitionKey: String, caseDefinitionId: CaseDefinitionId): FormFlowDefinition {
        return withLoggingContext(FormFlowDefinition::class.java.canonicalName to formFlowDefinitionKey) {
            findDefinition(FormFlowDefinitionId.existingId(formFlowDefinitionKey, caseDefinitionId))
        }
    }

    fun findDefinitionOrNull(formFlowDefinitionKey: String, caseDefinitionId: CaseDefinitionId): FormFlowDefinition? {
        return formFlowDefinitionRepository.findByIdOrNull(FormFlowDefinitionId.existingId(formFlowDefinitionKey, caseDefinitionId))
    }

    fun save(formFlowDefinition: FormFlowDefinition): FormFlowDefinition {
        return withLoggingContext(FormFlowDefinition::class.java.canonicalName to formFlowDefinition.id.toString()) {
            formFlowDefinitionRepository.save(formFlowDefinition)
        }
    }

    fun getInstanceById(formFlowInstanceId: FormFlowInstanceId): FormFlowInstance {
        return withLoggingContext(FormFlowDefinition::class.java.canonicalName to formFlowInstanceId.toString()) {
            formFlowInstanceRepository.getReferenceById(formFlowInstanceId)
        }
    }

    fun getByInstanceIdIfExists(formFlowInstanceId: FormFlowInstanceId): FormFlowInstance? {
        return withLoggingContext(FormFlowDefinition::class.java.canonicalName to formFlowInstanceId.toString()) {
            formFlowInstanceRepository.getReferenceById(formFlowInstanceId)
        }
    }

    fun save(formFlowInstance: FormFlowInstance): FormFlowInstance {
        return withLoggingContext(FormFlowDefinition::class.java.canonicalName to formFlowInstance.id.toString()) {
            formFlowInstanceRepository.save(formFlowInstance)
        }
    }

    fun findInstances(additionalProperties: Map<String, Any>): List<FormFlowInstance> {
        return formFlowAdditionalPropertiesSearchRepository.findInstances(additionalProperties)
    }

    fun getFormFlowStepTypeHandler(stepType: FormFlowStepType): FormFlowStepTypeHandler {
        return formFlowStepTypeHandlers.singleOrNull { it.getType() == stepType.name }
            ?: throw IllegalStateException("No formFlowStepTypeHandler found for type '${stepType.name}'")
    }

    fun getTypeProperties(stepInstance: FormFlowStepInstance): TypeProperties {
        return withLoggingContext(FormFlowStepInstance::class.java.canonicalName to stepInstance.id.toString()) {
            getFormFlowStepTypeHandler(stepInstance.definition.type).getTypeProperties(stepInstance)
        }
    }

    fun deleteByKeyAndsCaseDefinition(definitionKey: String, caseDefinitionId: CaseDefinitionId) {
        formFlowDefinitionRepository.deleteById(FormFlowDefinitionId.existingId(definitionKey, caseDefinitionId))
    }

    fun deleteAllByCaseDefinitionId(caseDefinitionId: CaseDefinitionId) {
        formFlowDefinitionRepository.deleteAllByIdCaseDefinitionId(caseDefinitionId)
    }

    fun getBreadcrumbs(instance: FormFlowInstance): List<FormFlowBreadcrumb> {
        return withLoggingContext(FormFlowInstance::class.java.canonicalName to instance.id.toString()) {
            val lastCompletedOrder = instance.getHistory()
                .filter { it.submissionData != null }
                .maxByOrNull { it.submissionOrder }
                ?.order ?: -1
            val historicBreadcrumbs = instance.getHistory()
                .map { FormFlowBreadcrumb.of(it, it.order <= lastCompletedOrder + 1, it.order <= lastCompletedOrder) }
            val futureBreadcrumbs = getFutureSteps(instance)
                .map { FormFlowBreadcrumb.of(it) }
            historicBreadcrumbs + futureBreadcrumbs
        }
    }

    private fun getFutureSteps(instance: FormFlowInstance): List<FormFlowStep> {
        return withLoggingContext(FormFlowInstance::class.java.canonicalName to instance.id.toString()) {
            getFutureSteps(instance.getHistory().last().definition)
        }
    }

    private fun getFutureSteps(step: FormFlowStep, result: MutableList<FormFlowStep> = mutableListOf()): List<FormFlowStep> {
        return withLoggingContext(FormFlowStep::class.java.canonicalName to step.id.toString()) {
            step.nextSteps.forEach { nextStep ->
                val futureStep = step.id.formFlowDefinition?.steps?.firstOrNull { it.id.key == nextStep.step }
                if (futureStep == null) {
                    return result
                } else if (!result.contains(futureStep)) {
                    result.add(futureStep)
                    return getFutureSteps(futureStep, result)
                }
            }
            result
        }
    }
}
