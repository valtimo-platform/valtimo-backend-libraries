/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ritense.valtimo.autoconfiguration

import com.ritense.valtimo.camunda.repository.CamundaExecutionRepository
import com.ritense.valtimo.camunda.repository.CamundaHistoricProcessInstanceRepository
import com.ritense.valtimo.camunda.repository.CamundaHistoricTaskInstanceRepository
import com.ritense.valtimo.camunda.repository.CamundaHistoricVariableInstanceRepository
import com.ritense.valtimo.camunda.repository.CamundaIdentityLinkRepository
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionRepository
import com.ritense.valtimo.camunda.repository.CamundaTaskRepository
import com.ritense.valtimo.camunda.repository.CamundaVariableInstanceRepository
import com.ritense.valtimo.camunda.service.CamundaHistoryService
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.camunda.service.CamundaRuntimeService
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(
    basePackageClasses = [
        CamundaExecutionRepository::class,
        CamundaHistoricProcessInstanceRepository::class,
        CamundaHistoricTaskInstanceRepository::class,
        CamundaHistoricVariableInstanceRepository::class,
        CamundaIdentityLinkRepository::class,
        CamundaProcessDefinitionRepository::class,
        CamundaTaskRepository::class,
        CamundaVariableInstanceRepository::class,
    ]
)
@EntityScan("com.ritense.valtimo.camunda.domain")
class ValtimoCamundaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CamundaHistoryService::class)
    fun camundaHistoryService(
        historyService: HistoryService,
        camundaHistoricProcessInstanceRepository: CamundaHistoricProcessInstanceRepository,
    ): CamundaHistoryService {
        return CamundaHistoryService(historyService, camundaHistoricProcessInstanceRepository)
    }

    @Bean
    @ConditionalOnMissingBean(CamundaRepositoryService::class)
    fun camundaRepositoryService(
        camundaProcessDefinitionRepository: CamundaProcessDefinitionRepository,
    ): CamundaRepositoryService {
        return CamundaRepositoryService(camundaProcessDefinitionRepository)
    }

    @Bean
    @ConditionalOnMissingBean(CamundaRuntimeService::class)
    fun camundaRuntimeService(
        runtimeService: RuntimeService,
        camundaVariableInstanceRepository: CamundaVariableInstanceRepository,
    ): CamundaRuntimeService {
        return CamundaRuntimeService(runtimeService, camundaVariableInstanceRepository)
    }

}