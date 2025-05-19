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

package com.ritense.processlink.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationService
import com.ritense.document.service.DocumentService
import com.ritense.processlink.autodeployment.ProcessLinkDeploymentApplicationReadyEventListener
import com.ritense.processlink.domain.SupportedProcessLinkTypeHandler
import com.ritense.processlink.exporter.ProcessLinkExporter
import com.ritense.processlink.importer.ProcessLinkImporter
import com.ritense.processlink.mapper.ProcessLinkMapper
import com.ritense.processlink.repository.ProcessLinkRepository
import com.ritense.processlink.security.config.ProcessLinkHttpSecurityConfigurer
import com.ritense.processlink.service.CopyProcessLinkOnProcessDeploymentListener
import com.ritense.processlink.service.ProcessLinkActivityHandler
import com.ritense.processlink.service.ProcessLinkActivityService
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.processlink.web.rest.ProcessLinkResource
import com.ritense.processlink.web.rest.ProcessLinkTaskResource
import com.ritense.valtimo.autoconfiguration.ValtimoCamundaAutoConfiguration
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.event.ProcessDefinitionDeployedEvent
import com.ritense.valtimo.service.CamundaProcessService
import com.ritense.valtimo.service.CamundaTaskService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order
import org.springframework.core.env.Environment
import org.springframework.core.io.ResourceLoader
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@AutoConfiguration
@EnableJpaRepositories(
    basePackageClasses = [
        ProcessLinkRepository::class,
    ]
)
@EntityScan(basePackages = ["com.ritense.processlink.domain"])
@AutoConfigureAfter(ValtimoCamundaAutoConfiguration::class)
class ProcessLinkAutoConfiguration {

    @Order(420)
    @Bean
    @ConditionalOnMissingBean(ProcessLinkHttpSecurityConfigurer::class)
    fun processLinkHttpSecurityConfigurer(): ProcessLinkHttpSecurityConfigurer {
        return ProcessLinkHttpSecurityConfigurer()
    }

    @Bean
    @ConditionalOnMissingBean(ProcessLinkService::class)
    fun processLinkService(
        processLinkRepository: ProcessLinkRepository,
        processLinkMappers: List<ProcessLinkMapper>,
        processLinkTypes: List<SupportedProcessLinkTypeHandler>,
        camundaRepositoryService: CamundaRepositoryService,
    ): ProcessLinkService {
        return ProcessLinkService(
            processLinkRepository,
            processLinkMappers,
            processLinkTypes,
            camundaRepositoryService
        )
    }

    @Bean
    @ConditionalOnMissingBean(ProcessLinkActivityService::class)
    fun processLinkTaskService(
        processLinkService: ProcessLinkService,
        taskService: CamundaTaskService,
        processLinkActivityHandlers: List<ProcessLinkActivityHandler<*>>,
        authorizationService: AuthorizationService,
        camundaRepositoryService: CamundaRepositoryService,
        documentService: DocumentService,
        camundaTaskService: CamundaTaskService,
        camundaProcessService: CamundaProcessService
    ): ProcessLinkActivityService {
        return ProcessLinkActivityService(
            processLinkService,
            taskService,
            processLinkActivityHandlers,
            authorizationService,
            camundaRepositoryService,
            documentService,
            camundaTaskService,
            camundaProcessService
        )
    }

    @Bean
    @ConditionalOnMissingBean(ProcessLinkTaskResource::class)
    @ConditionalOnBean(ProcessLinkActivityService::class)
    fun processLinkTaskResource(
        processLinkActivityService: ProcessLinkActivityService
    ): ProcessLinkTaskResource {
        return ProcessLinkTaskResource(processLinkActivityService)
    }

    @Bean
    @ConditionalOnMissingBean(ProcessLinkResource::class)
    fun processLinkProcessLinkResource(
        processLinkService: ProcessLinkService,
        processLinkMappers: List<ProcessLinkMapper>,
    ): ProcessLinkResource {
        return ProcessLinkResource(processLinkService, processLinkMappers)
    }

    @Bean
    @ConditionalOnMissingBean(CopyProcessLinkOnProcessDeploymentListener::class)
    @ConditionalOnClass(ProcessDefinitionDeployedEvent::class)
    fun copyProcessLinkOnProcessDeploymentListener(
        processLinkRepository: ProcessLinkRepository,
    ): CopyProcessLinkOnProcessDeploymentListener {
        return CopyProcessLinkOnProcessDeploymentListener(
            processLinkRepository,
        )
    }

    @Bean
    @ConditionalOnMissingBean(ProcessLinkDeploymentApplicationReadyEventListener::class)
    fun processLinkDeploymentApplicationReadyEventListener(
        resourceLoader: ResourceLoader,
        processLinkImporter: ProcessLinkImporter,
        objectMapper: ObjectMapper,
        environment: Environment
    ): ProcessLinkDeploymentApplicationReadyEventListener {
        return ProcessLinkDeploymentApplicationReadyEventListener(
            resourceLoader,
            processLinkImporter,
            objectMapper,
            environment
        )
    }

    @Bean
    @ConditionalOnMissingBean(ProcessLinkExporter::class)
    fun processLinkExporter(
        objectMapper: ObjectMapper,
        processLinkService: ProcessLinkService,
        repositoryService: CamundaRepositoryService
    ) = ProcessLinkExporter(
        objectMapper,
        processLinkService,
        repositoryService
    )

    @Bean
    @ConditionalOnMissingBean(ProcessLinkImporter::class)
    fun processLinkImporter(
        processLinkService: ProcessLinkService,
        repositoryService: CamundaRepositoryService,
        objectMapper: ObjectMapper
    ) = ProcessLinkImporter(
        processLinkService,
        repositoryService,
        objectMapper
    )

}
