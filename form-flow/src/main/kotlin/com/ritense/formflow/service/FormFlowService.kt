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

package com.ritense.formflow.service

import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.domain.definition.FormFlowStep
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.domain.instance.FormFlowInstanceId
import com.ritense.formflow.expression.ExpressionProcessorFactory
import com.ritense.formflow.repository.FormFlowDefinitionRepository
import com.ritense.formflow.repository.FormFlowInstanceRepository

class FormFlowService(
    private val formFlowDefinitionRepository: FormFlowDefinitionRepository,
    private val formFlowInstanceRepository: FormFlowInstanceRepository,
    private val expressionProcessorFactory: ExpressionProcessorFactory
) {

    fun findLatestDefinitionByKey(formFlowKey: String): FormFlowDefinition? {
        return formFlowDefinitionRepository.findFirstByIdKeyOrderByIdVersionDesc(formFlowKey)
    }

    fun getDefinitionById(formFlowDefinitionId: FormFlowDefinitionId): FormFlowDefinition {
        return formFlowDefinitionRepository.getById(formFlowDefinitionId)
    }

    fun open(formFlowInstanceId: FormFlowInstanceId) {
        processExpressions(formFlowInstanceId, FormFlowStep::onOpen)
    }

    fun complete(formFlowInstanceId: FormFlowInstanceId) {
        processExpressions(formFlowInstanceId, FormFlowStep::onComplete)
    }

    private fun processExpressions(formFlowInstanceId: FormFlowInstanceId, expressionist: (FormFlowStep)-> List<String>?) {
        val formFlowInstance = formFlowInstanceRepository.getById(formFlowInstanceId)
        val currentStep = formFlowInstance.getCurrentStep()
        val expressionProcessor = expressionProcessorFactory.create(mapOf("step" to currentStep))

        val stepDefinition = currentStep.definition
        expressionist(stepDefinition)?.forEach { expression ->
            expressionProcessor.process<Any>(expression)
        }
    }

    fun save(formFlowDefinition: FormFlowDefinition) {
        formFlowDefinitionRepository.findById(formFlowDefinition.id).ifPresentOrElse({
            throw UnsupportedOperationException("Failed to save From Flow. Form Flow already exists: ${formFlowDefinition.id}")
        }, {
            formFlowDefinitionRepository.save(formFlowDefinition)
        })
    }

    fun getInstanceById(formFlowInstanceId: FormFlowInstanceId): FormFlowInstance {
        return formFlowInstanceRepository.getById(formFlowInstanceId)
    }

    fun save(formFlowInstance: FormFlowInstance) {
        formFlowInstanceRepository.save(formFlowInstance)
    }
}