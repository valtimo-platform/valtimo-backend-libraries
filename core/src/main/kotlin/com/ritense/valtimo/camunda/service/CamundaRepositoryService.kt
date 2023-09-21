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

import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionRepository
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byId
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byKey
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byLatestVersion
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.transaction.annotation.Transactional

open class CamundaRepositoryService(
    private val camundaProcessDefinitionRepository: CamundaProcessDefinitionRepository,
    private val authorizationService: AuthorizationService
) {

    @Transactional(readOnly = true)
    open fun findProcessDefinitionById(processDefinitionId: String): CamundaProcessDefinition? {
        denyAuthorization()
        return runWithoutAuthorization{ findProcessDefinition(byId(processDefinitionId)) }
    }

    @Transactional(readOnly = true)
    open fun findLatestProcessDefinition(processDefinitionKey: String): CamundaProcessDefinition? {
        denyAuthorization()
        return runWithoutAuthorization { findProcessDefinition(byKey(processDefinitionKey).and(byLatestVersion())) }
    }

    @Transactional(readOnly = true)
    open fun findProcessDefinitions(
        specification: Specification<CamundaProcessDefinition>,
        sort: Sort
    ): List<CamundaProcessDefinition> {
        denyAuthorization()
        return camundaProcessDefinitionRepository.findAll(specification, sort)
    }

    @Transactional(readOnly = true)
    open fun findProcessDefinitions(
        specification: Specification<CamundaProcessDefinition>
    ): List<CamundaProcessDefinition> {
        denyAuthorization()
        return camundaProcessDefinitionRepository.findAll(specification)
    }

    @Transactional(readOnly = true)
    open fun findProcessDefinition(specification: Specification<CamundaProcessDefinition>): CamundaProcessDefinition? {
        denyAuthorization()
        return camundaProcessDefinitionRepository.findOne(specification).orElse(null)
    }

    @Transactional(readOnly = true)
    open fun countProcessDefinitions(specification: Specification<CamundaProcessDefinition>): Long {
        denyAuthorization()
        return camundaProcessDefinitionRepository.count(specification)
    }

    @Transactional(readOnly = true)
    open fun processDefinitionExists(specification: Specification<CamundaProcessDefinition>): Boolean {
        denyAuthorization()
        return camundaProcessDefinitionRepository.exists(specification)
    }

    private fun denyAuthorization() {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                CamundaProcessDefinition::class.java,
                Action.deny()
            )
        )
    }
}