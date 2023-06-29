/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.camunda.service

import com.ritense.valtimo.camunda.domain.CamundaVariableInstance
import com.ritense.valtimo.camunda.repository.CamundaVariableInstanceRepository
import com.ritense.valtimo.camunda.repository.CamundaVariableInstanceSpecificationHelper.Companion.NAME
import com.ritense.valtimo.camunda.repository.CamundaVariableInstanceSpecificationHelper.Companion.byNameIn
import com.ritense.valtimo.camunda.repository.CamundaVariableInstanceSpecificationHelper.Companion.byProcessInstanceId
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification

open class CamundaRuntimeService(
    private val runtimeService: RuntimeService,
    private val camundaVariableInstanceRepository: CamundaVariableInstanceRepository
) {

    fun findVariableInstances(
        specification: Specification<CamundaVariableInstance>,
        sort: Sort
    ): List<CamundaVariableInstance> =
        camundaVariableInstanceRepository.findAll(specification, sort)

    open fun getVariables(processInstanceId: String, variableNames: List<String>): Map<String, Any?> {
        val variableInstances = findVariableInstances(
            byProcessInstanceId(processInstanceId).and(byNameIn(*variableNames.toTypedArray())),
            Sort.by(Sort.Direction.DESC, NAME)
        )
        return variableInstances
            .filter { variableInstance: CamundaVariableInstance -> variableInstance.getValue() != null }
            .associate { obj -> obj.name to obj.getValue() }
    }

    fun findProcessInstanceById(processInstanceId: String): ProcessInstance? =
        runtimeService
            .createProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult()
}