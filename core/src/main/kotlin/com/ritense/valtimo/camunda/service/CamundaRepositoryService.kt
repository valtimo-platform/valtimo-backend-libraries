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

class CamundaRepositoryService(
    private val repositoryService: RepositoryService,
    private val camundaProcessDefinitionRepository: CamundaProcessDefinitionRepository
) {

    fun findById(processDefinitionId: String) = find(byId(processDefinitionId))
    fun findLatest(processDefinitionKey: String) = find(byKey(processDefinitionKey).and(byLatestVersion()))

    fun findAll(specification: Specification<CamundaProcessDefinition>, sort: Sort) =
        camundaProcessDefinitionRepository.findAll(specification, sort)

    fun findAll(specification: Specification<CamundaProcessDefinition>) =
        camundaProcessDefinitionRepository.findAll(specification)

    fun find(specification: Specification<CamundaProcessDefinition>) =
        camundaProcessDefinitionRepository.findOne(specification).orElse(null)

    fun count(specification: Specification<CamundaProcessDefinition>) =
        camundaProcessDefinitionRepository.count(specification)
}