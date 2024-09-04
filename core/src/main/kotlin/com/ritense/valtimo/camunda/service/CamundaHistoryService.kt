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

import com.ritense.authorization.Action.Companion.deny
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.valtimo.camunda.domain.CamundaHistoricProcessInstance
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.camunda.repository.CamundaHistoricProcessInstanceRepository
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.camunda.bpm.engine.HistoryService
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@SkipComponentScan
class CamundaHistoryService(
    private val historyService: HistoryService,
    private val camundaHistoricProcessInstanceRepository: CamundaHistoricProcessInstanceRepository,
    private val authorizationService: AuthorizationService
) {

    @Transactional(readOnly = true)
    fun findHistoricProcessInstances(
        specification: Specification<CamundaHistoricProcessInstance>
    ): List<CamundaHistoricProcessInstance> {
        denyAuthorization()
        return camundaHistoricProcessInstanceRepository.findAll(specification)
    }

    @Transactional(readOnly = true)
    fun findHistoricProcessInstance(
        specification: Specification<CamundaHistoricProcessInstance>
    ): CamundaHistoricProcessInstance? {
        denyAuthorization()
        return camundaHistoricProcessInstanceRepository.findOne(specification).orElse(null)
    }

    @Transactional(readOnly = true)
    fun countHistoricProcessInstances(specification: Specification<CamundaHistoricProcessInstance>): Long {
        denyAuthorization()
        return camundaHistoricProcessInstanceRepository.count(specification)
    }

    private fun denyAuthorization() {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                CamundaProcessDefinition::class.java,
                deny()
            )
        )
    }
}