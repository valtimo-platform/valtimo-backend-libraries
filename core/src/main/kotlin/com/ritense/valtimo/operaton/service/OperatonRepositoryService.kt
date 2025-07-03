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

package com.ritense.valtimo.operaton.service

import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.valtimo.operaton.domain.OperatonDecisionDefinition
import com.ritense.valtimo.operaton.domain.OperatonProcessDefinition
import com.ritense.valtimo.operaton.repository.OperatonDecisionDefinitionRepository
import com.ritense.valtimo.operaton.repository.OperatonProcessDefinitionRepository
import com.ritense.valtimo.operaton.repository.OperatonProcessDefinitionSpecificationHelper.Companion.byId
import com.ritense.valtimo.operaton.repository.OperatonProcessDefinitionSpecificationHelper.Companion.byKey
import com.ritense.valtimo.operaton.repository.OperatonProcessDefinitionSpecificationHelper.Companion.byLatestVersion
import com.ritense.valtimo.operaton.repository.OperatonProcessDefinitionSpecificationHelper.Companion.byVersion
import com.ritense.valtimo.operaton.repository.OperatonProcessDefinitionSpecificationHelper.Companion.byVersionTag
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.operaton.bpm.engine.RepositoryService
import org.operaton.bpm.model.bpmn.instance.CallActivity
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@SkipComponentScan
class OperatonRepositoryService(
    private val operatonProcessDefinitionRepository: OperatonProcessDefinitionRepository,
    private val operatonDecisionDefinitionRepository: OperatonDecisionDefinitionRepository,
    private val authorizationService: AuthorizationService,
    private val repositoryService: RepositoryService,
) {

    @Transactional(readOnly = true)
    fun findProcessDefinitionById(processDefinitionId: String): OperatonProcessDefinition? {
        denyAuthorization()
        return runWithoutAuthorization{ findProcessDefinition(byId(processDefinitionId)) }
    }

    @Transactional(readOnly = true)
    fun findLatestProcessDefinition(processDefinitionKey: String): OperatonProcessDefinition? {
        denyAuthorization()
        return runWithoutAuthorization { findProcessDefinition(byKey(processDefinitionKey).and(byLatestVersion())) }
    }

    @Transactional(readOnly = true)
    fun findProcessDefinitions(
        specification: Specification<OperatonProcessDefinition>,
        sort: Sort
    ): List<OperatonProcessDefinition> {
        denyAuthorization()
        return operatonProcessDefinitionRepository.findAll(specification, sort)
    }

    @Transactional(readOnly = true)
    fun findProcessDefinitions(
        specification: Specification<OperatonProcessDefinition>
    ): List<OperatonProcessDefinition> {
        denyAuthorization()
        return operatonProcessDefinitionRepository.findAll(specification)
    }

    @Transactional(readOnly = true)
    fun findProcessDefinition(specification: Specification<OperatonProcessDefinition>): OperatonProcessDefinition? {
        denyAuthorization()
        return operatonProcessDefinitionRepository.findOne(specification).orElse(null)
    }

    @Transactional(readOnly = true)
    fun findDecisionDefinition(specification: Specification<OperatonDecisionDefinition>): OperatonDecisionDefinition? {
        denyAuthorization()
        return operatonDecisionDefinitionRepository.findOne(specification).orElse(null)
    }

    @Transactional(readOnly = true)
    fun countProcessDefinitions(specification: Specification<OperatonProcessDefinition>): Long {
        denyAuthorization()
        return operatonProcessDefinitionRepository.count(specification)
    }

    @Transactional(readOnly = true)
    fun processDefinitionExists(specification: Specification<OperatonProcessDefinition>): Boolean {
        denyAuthorization()
        return operatonProcessDefinitionRepository.exists(specification)
    }

    @Transactional(readOnly = true)
    fun findLinkedProcessDefinitions(specification: Specification<OperatonProcessDefinition>): List<OperatonProcessDefinition> {
        denyAuthorization()
        val linkedProcessDefinitions = mutableListOf<OperatonProcessDefinition>()
        operatonProcessDefinitionRepository.findAll(specification)
            .forEach { findLinkedProcessDefinitions(it, linkedProcessDefinitions) }
        return linkedProcessDefinitions
    }

    private fun findLinkedProcessDefinitions(
        processDefinition: OperatonProcessDefinition,
        linkedProcessDefinitions: MutableList<OperatonProcessDefinition> = mutableListOf()
    ) {
        linkedProcessDefinitions.add(processDefinition)
        val bpmnModelInstance = repositoryService.getBpmnModelInstance(processDefinition.id)
        bpmnModelInstance.getModelElementsByType(CallActivity::class.java)
            .mapNotNull {
                val spec = byKey(it.calledElement)
                when (it.operatonCalledElementBinding) {
                    "version" -> findProcessDefinition(spec.and(byVersion(it.operatonCalledElementVersion.toInt())))
                    "versionTag" -> findProcessDefinition(spec.and(byVersionTag(it.operatonCalledElementVersionTag)))
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
                OperatonProcessDefinition::class.java,
                Action.deny()
            )
        )
    }
}