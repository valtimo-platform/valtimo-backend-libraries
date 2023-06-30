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

import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionRepository
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byId
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byKey
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byLatestVersion
import org.camunda.bpm.engine.RepositoryService
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification

open class CamundaRepositoryService(
    private val camundaProcessDefinitionRepository: CamundaProcessDefinitionRepository
) {

    fun findProcessDefinitionById(processDefinitionId: String): CamundaProcessDefinition? =
        findProcessDefinition(byId(processDefinitionId))

    fun findLatestProcessDefinition(processDefinitionKey: String): CamundaProcessDefinition? =
        findProcessDefinition(byKey(processDefinitionKey).and(byLatestVersion()))

    fun findProcessDefinitions(
        specification: Specification<CamundaProcessDefinition>,
        sort: Sort
    ): List<CamundaProcessDefinition> =
        camundaProcessDefinitionRepository.findAll(specification, sort)

    fun findProcessDefinitions(specification: Specification<CamundaProcessDefinition>): List<CamundaProcessDefinition> =
        camundaProcessDefinitionRepository.findAll(specification)

    fun findProcessDefinition(specification: Specification<CamundaProcessDefinition>): CamundaProcessDefinition? =
        camundaProcessDefinitionRepository.findOne(specification).orElse(null)

    fun countProcessDefinitions(specification: Specification<CamundaProcessDefinition>) =
        camundaProcessDefinitionRepository.count(specification)

    fun processDefinitionExists(specification: Specification<CamundaProcessDefinition>) =
        camundaProcessDefinitionRepository.exists(specification)
}