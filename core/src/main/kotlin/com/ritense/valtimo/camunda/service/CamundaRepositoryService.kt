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
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byVersion
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byVersionTag
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.model.bpmn.instance.CallActivity
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@SkipComponentScan
class CamundaRepositoryService(
    private val camundaProcessDefinitionRepository: CamundaProcessDefinitionRepository,
    private val authorizationService: AuthorizationService,
    private val repositoryService: RepositoryService,
) {

    @Transactional(readOnly = true)
    fun findProcessDefinitionById(processDefinitionId: String): CamundaProcessDefinition? {
        denyAuthorization()
        return runWithoutAuthorization{ findProcessDefinition(byId(processDefinitionId)) }
    }

    @Transactional(readOnly = true)
    fun findLatestProcessDefinition(processDefinitionKey: String): CamundaProcessDefinition? {
        denyAuthorization()
        return runWithoutAuthorization { findProcessDefinition(byKey(processDefinitionKey).and(byLatestVersion())) }
    }

    @Transactional(readOnly = true)
    fun findProcessDefinitions(
        specification: Specification<CamundaProcessDefinition>,
        sort: Sort
    ): List<CamundaProcessDefinition> {
        denyAuthorization()
        return camundaProcessDefinitionRepository.findAll(specification, sort)
    }

    @Transactional(readOnly = true)
    fun findProcessDefinitions(
        specification: Specification<CamundaProcessDefinition>
    ): List<CamundaProcessDefinition> {
        denyAuthorization()
        return camundaProcessDefinitionRepository.findAll(specification)
    }

    @Transactional(readOnly = true)
    fun findProcessDefinition(specification: Specification<CamundaProcessDefinition>): CamundaProcessDefinition? {
        denyAuthorization()
        return camundaProcessDefinitionRepository.findOne(specification).orElse(null)
    }

    @Transactional(readOnly = true)
    fun countProcessDefinitions(specification: Specification<CamundaProcessDefinition>): Long {
        denyAuthorization()
        return camundaProcessDefinitionRepository.count(specification)
    }

    @Transactional(readOnly = true)
    fun processDefinitionExists(specification: Specification<CamundaProcessDefinition>): Boolean {
        denyAuthorization()
        return camundaProcessDefinitionRepository.exists(specification)
    }

    @Transactional(readOnly = true)
    fun findLinkedProcessDefinitions(specification: Specification<CamundaProcessDefinition>): List<CamundaProcessDefinition> {
        denyAuthorization()
        val linkedProcessDefinitions = mutableListOf<CamundaProcessDefinition>()
        camundaProcessDefinitionRepository.findAll(specification)
            .forEach { findLinkedProcessDefinitions(it, linkedProcessDefinitions) }
        return linkedProcessDefinitions
    }

    private fun findLinkedProcessDefinitions(
        processDefinition: CamundaProcessDefinition,
        linkedProcessDefinitions: MutableList<CamundaProcessDefinition> = mutableListOf()
    ) {
        linkedProcessDefinitions.add(processDefinition)
        val bpmnModelInstance = repositoryService.getBpmnModelInstance(processDefinition.id)
        bpmnModelInstance.getModelElementsByType(CallActivity::class.java)
            .mapNotNull {
                val spec = byKey(it.calledElement)
                when (it.camundaCalledElementBinding) {
                    "version" -> findProcessDefinition(spec.and(byVersion(it.camundaCalledElementVersion.toInt())))
                    "versionTag" -> findProcessDefinition(spec.and(byVersionTag(it.camundaCalledElementVersionTag)))
                    "deployment" -> null
                    else -> findProcessDefinition(spec.and(byLatestVersion()))
                }
            }
            .filter { found -> !linkedProcessDefinitions.any { linked -> linked.id == found.id  } }
            .forEach { findLinkedProcessDefinitions(it, linkedProcessDefinitions) }
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