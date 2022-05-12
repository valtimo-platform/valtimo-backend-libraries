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
import com.ritense.formflow.domain.definition.configuration.FormFlowStepType
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.domain.instance.FormFlowInstanceId
import com.ritense.formflow.domain.instance.FormFlowStepInstance
import com.ritense.formflow.handler.FormFlowStepTypeHandler
import com.ritense.formflow.handler.TypeProperties
import com.ritense.formflow.repository.FormFlowDefinitionRepository
import com.ritense.formflow.repository.FormFlowInstanceRepository

class FormFlowService(
    private val formFlowDefinitionRepository: FormFlowDefinitionRepository,
    private val formFlowInstanceRepository: FormFlowInstanceRepository,
    private val formFlowStepTypeHandlers: List<FormFlowStepTypeHandler>
) {

    fun getFormFlowDefinitions(): List<FormFlowDefinition> {
        return formFlowDefinitionRepository.findAll()
    }

    fun findLatestDefinitionByKey(formFlowKey: String): FormFlowDefinition? {
        return formFlowDefinitionRepository.findFirstByIdKeyOrderByIdVersionDesc(formFlowKey)
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

    fun getByInstanceIdIfExists(formFlowInstanceId: FormFlowInstanceId): FormFlowInstance? {
        return formFlowInstanceRepository.getById(formFlowInstanceId)
    }

    fun save(formFlowInstance: FormFlowInstance) {
        formFlowInstanceRepository.save(formFlowInstance)
    }

    fun getFormFlowStepTypeHandler(stepType: FormFlowStepType): FormFlowStepTypeHandler {
        return formFlowStepTypeHandlers.singleOrNull { it.getType() == stepType.name }
            ?: throw IllegalStateException("No formFlowStepTypeHandler found for type '${stepType.name}'")
    }

    fun getTypeProperties(
        stepInstance: FormFlowStepInstance,
        additionalParameters: Map<String, Any> = emptyMap()
    ): TypeProperties {
        return getFormFlowStepTypeHandler(stepInstance.definition.type)
            .getTypeProperties(stepInstance, additionalParameters)
    }
}
