/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

import com.ritense.authorization.AuthorizationService
import com.ritense.valtimo.ValtimoApplicationPropertyService
import com.ritense.valtimo.camunda.authorization.CamundaExecutionProcessDefinitionMapper
import com.ritense.valtimo.camunda.authorization.CamundaExecutionSpecificationFactory
import com.ritense.valtimo.camunda.authorization.CamundaIdentityLinkSpecificationFactory
import com.ritense.valtimo.camunda.authorization.CamundaProcessDefinitionSpecificationFactory
import com.ritense.valtimo.camunda.authorization.CamundaTaskSpecificationFactory
import com.ritense.valtimo.camunda.repository.CamundaBytearrayRepository
import com.ritense.valtimo.camunda.repository.CamundaExecutionRepository
import com.ritense.valtimo.camunda.repository.CamundaHistoricProcessInstanceRepository
import com.ritense.valtimo.camunda.repository.CamundaHistoricTaskInstanceRepository
import com.ritense.valtimo.camunda.repository.CamundaHistoricVariableInstanceRepository
import com.ritense.valtimo.camunda.repository.CamundaIdentityLinkRepository
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionRepository
import com.ritense.valtimo.camunda.repository.CamundaTaskIdentityLinkMapper
import com.ritense.valtimo.camunda.repository.CamundaTaskRepository
import com.ritense.valtimo.camunda.repository.CamundaVariableInstanceRepository
import com.ritense.valtimo.camunda.service.CamundaContextService
import com.ritense.valtimo.camunda.service.CamundaHistoryService
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.camunda.service.CamundaRuntimeService
import com.ritense.valtimo.contract.config.ValtimoProperties
import com.ritense.valtimo.contract.database.QueryDialectHelper
import com.ritense.valtimo.repository.ValtimoApplicationPropertyRepository
import com.ritense.valtimo.service.CamundaTaskService
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Lazy
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@AutoConfiguration
@EnableJpaRepositories(
    basePackageClasses = [
        CamundaBytearrayRepository::class,
        CamundaExecutionRepository::class,
        CamundaHistoricProcessInstanceRepository::class,
        CamundaHistoricTaskInstanceRepository::class,
        CamundaHistoricVariableInstanceRepository::class,
        CamundaIdentityLinkRepository::class,
        CamundaProcessDefinitionRepository::class,
        CamundaTaskRepository::class,
        CamundaVariableInstanceRepository::class
    ]
)
@EntityScan(
    basePackages = [
        "com.ritense.valtimo.camunda.domain",
        "com.ritense.valtimo.domain"
    ]
)
class ValtimoCamundaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CamundaContextService::class)
    fun camundaContextService(
        processEngineConfiguration: ProcessEngineConfigurationImpl,
    ): CamundaContextService {
        return CamundaContextService(processEngineConfiguration)
    }

    @Bean
    @ConditionalOnMissingBean(CamundaHistoryService::class)
    fun camundaHistoryService(
        historyService: HistoryService,
        camundaHistoricProcessInstanceRepository: CamundaHistoricProcessInstanceRepository,
        authorizationService: AuthorizationService
    ): CamundaHistoryService {
        return CamundaHistoryService(historyService, camundaHistoricProcessInstanceRepository, authorizationService)
    }

    @Bean
    @ConditionalOnMissingBean(CamundaRepositoryService::class)
    fun camundaRepositoryService(
        camundaProcessDefinitionRepository: CamundaProcessDefinitionRepository,
        authorizationService: AuthorizationService,
        repositoryService: RepositoryService,
    ): CamundaRepositoryService {
        return CamundaRepositoryService(
            camundaProcessDefinitionRepository,
            authorizationService,
            repositoryService
        )
    }

    @Bean
    @ConditionalOnMissingBean(CamundaRuntimeService::class)
    fun camundaRuntimeService(
        runtimeService: RuntimeService,
        camundaVariableInstanceRepository: CamundaVariableInstanceRepository,
        camundaIdentityLinkRepository: CamundaIdentityLinkRepository,
        authorizationService: AuthorizationService
    ): CamundaRuntimeService {
        return CamundaRuntimeService(
            runtimeService,
            camundaVariableInstanceRepository,
            camundaIdentityLinkRepository,
            authorizationService
        )
    }

    @Bean
    @ConditionalOnMissingBean(CamundaTaskSpecificationFactory::class)
    @ConditionalOnBean(AuthorizationService::class)
    fun camundaTaskSpecificationFactory(
        @Lazy camundaTaskService: CamundaTaskService,
        queryDialectHelper: QueryDialectHelper
    ): CamundaTaskSpecificationFactory {
        return CamundaTaskSpecificationFactory(camundaTaskService, queryDialectHelper)
    }

    @Bean
    @ConditionalOnMissingBean(CamundaIdentityLinkSpecificationFactory::class)
    @ConditionalOnBean(AuthorizationService::class)
    fun camundaIdentityLinkSpecificationFactory(
        @Lazy camundaRuntimeService: CamundaRuntimeService,
        queryDialectHelper: QueryDialectHelper
    ): CamundaIdentityLinkSpecificationFactory {
        return CamundaIdentityLinkSpecificationFactory(camundaRuntimeService, queryDialectHelper)
    }

    @Bean
    @ConditionalOnMissingBean(CamundaExecutionSpecificationFactory::class)
    @ConditionalOnBean(AuthorizationService::class)
    fun camundaExecutionSpecificationFactory(
        repository: CamundaExecutionRepository,
        queryDialectHelper: QueryDialectHelper
    ): CamundaExecutionSpecificationFactory {
        return CamundaExecutionSpecificationFactory(repository, queryDialectHelper)
    }

    @Bean
    @ConditionalOnMissingBean(CamundaProcessDefinitionSpecificationFactory::class)
    @ConditionalOnBean(AuthorizationService::class)
    fun camundaProcessDefinitionSpecificationFactory(
        repository: CamundaProcessDefinitionRepository,
        queryDialectHelper: QueryDialectHelper
    ): CamundaProcessDefinitionSpecificationFactory {
        return CamundaProcessDefinitionSpecificationFactory(repository, queryDialectHelper)
    }

    @Bean
    @ConditionalOnMissingBean(CamundaExecutionProcessDefinitionMapper::class)
    @ConditionalOnBean(AuthorizationService::class)
    fun camundaExecutionProcessDefinitionMapper() = CamundaExecutionProcessDefinitionMapper()


    @Bean
    @ConditionalOnMissingBean(CamundaTaskIdentityLinkMapper::class)
    fun camundaTaskIdentityLinkMapper(): CamundaTaskIdentityLinkMapper {
        return CamundaTaskIdentityLinkMapper()
    }

    @Bean
    fun valtimoApplicationPropertyService(
        repository: ValtimoApplicationPropertyRepository,
        valtimoProperties: ValtimoProperties
    ): ValtimoApplicationPropertyService = ValtimoApplicationPropertyService(repository, valtimoProperties)

}