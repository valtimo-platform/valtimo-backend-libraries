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

package com.ritense.valtimo.formflow.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.document.service.DocumentService
import com.ritense.form.service.PrefillFormService
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formflow.service.FormFlowDeploymentService
import com.ritense.formflow.service.FormFlowService
import com.ritense.outbox.OutboxService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processlink.service.ProcessLinkActivityHandler
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.formflow.FormFlowProcessLinkActivityHandler
import com.ritense.valtimo.formflow.FormFlowTaskOpenResultProperties
import com.ritense.valtimo.formflow.common.ValtimoFormFlow
import com.ritense.valtimo.formflow.event.FormFlowStepCompletedEventListener
import com.ritense.valtimo.formflow.exporter.FormFlowDefinitionExporter
import com.ritense.valtimo.formflow.handler.FormFlowStepTypeCustomComponentHandler
import com.ritense.valtimo.formflow.handler.FormFlowStepTypeFormHandler
import com.ritense.valtimo.formflow.importer.FormFlowDefinitionImporter
import com.ritense.valtimo.formflow.mapper.FormFlowProcessLinkMapper
import com.ritense.valtimo.formflow.repository.FormFlowProcessLinkRepository
import com.ritense.valtimo.formflow.security.ValtimoFormFlowHttpSecurityConfigurer
import com.ritense.valtimo.formflow.service.FormFlowSupportedProcessLinksHandler
import com.ritense.valtimo.formflow.service.FormFlowValtimoService
import com.ritense.valtimo.formflow.web.rest.FormFlowManagementResource
import com.ritense.valtimo.formflow.web.rest.FormFlowResource
import com.ritense.valtimo.formflow.web.rest.ProcessLinkFormFlowDefinitionResource
import com.ritense.valtimo.service.CamundaTaskService
import com.ritense.valueresolver.ValueResolverService
import org.camunda.bpm.engine.RuntimeService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@AutoConfiguration
@EnableJpaRepositories(
    basePackageClasses = [FormFlowProcessLinkRepository::class]
)
@EntityScan(basePackages = ["com.ritense.valtimo.formflow.domain"])
class FormFlowValtimoAutoConfiguration {

    @Bean
    fun formFlowProcessLinkTaskProvider(
        formFlowService: FormFlowService,
        repositoryService: CamundaRepositoryService,
        documentService: DocumentService,
        runtimeService: RuntimeService,
    ): ProcessLinkActivityHandler<FormFlowTaskOpenResultProperties> {
        return FormFlowProcessLinkActivityHandler(
            formFlowService,
            repositoryService,
            documentService,
            runtimeService
        )
    }

    @Bean
    @ConditionalOnMissingBean(ProcessLinkFormFlowDefinitionResource::class)
    fun processLinkFormFlowDefinitionResource(formFlowService: FormFlowService): ProcessLinkFormFlowDefinitionResource {
        return ProcessLinkFormFlowDefinitionResource(formFlowService)
    }

    @Bean
    @ConditionalOnMissingBean(FormFlowResource::class)
    fun formFlowResource(
        formFlowService: FormFlowService,
        formFlowValtimoService: FormFlowValtimoService,
    ): FormFlowResource {
        return FormFlowResource(
            formFlowService,
            formFlowValtimoService
        )
    }

    @Bean
    @ConditionalOnMissingBean(FormFlowManagementResource::class)
    fun formFlowManagementResource(
        formFlowService: FormFlowService,
        formFlowDeploymentService: FormFlowDeploymentService,
    ): FormFlowManagementResource {
        return FormFlowManagementResource(formFlowService, formFlowDeploymentService)
    }

    @Bean
    @Order(270)
    @ConditionalOnMissingBean(ValtimoFormFlowHttpSecurityConfigurer::class)
    fun valtimoFormFlowHttpSecurityConfigurer(): ValtimoFormFlowHttpSecurityConfigurer {
        return ValtimoFormFlowHttpSecurityConfigurer()
    }

    @Bean
    @ConditionalOnMissingBean(FormFlowStepTypeFormHandler::class)
    fun formFlowStepTypeFormHandler(
        formIoFormDefinitionService: FormIoFormDefinitionService,
        prefillFormService: PrefillFormService,
        documentService: DocumentService,
        objectMapper: ObjectMapper
    ): FormFlowStepTypeFormHandler {
        return FormFlowStepTypeFormHandler(
            formIoFormDefinitionService,
            prefillFormService,
            documentService,
            objectMapper
        )
    }

    @Bean
    @ConditionalOnMissingBean(FormFlowStepTypeCustomComponentHandler::class)
    fun formFlowStepTypeCustomComponentHandler(): FormFlowStepTypeCustomComponentHandler {
        return FormFlowStepTypeCustomComponentHandler()
    }

    @Bean
    @ConditionalOnMissingBean(ValtimoFormFlow::class)
    fun valtimoFormFlow(
        taskService: CamundaTaskService,
        objectMapper: ObjectMapper,
        valueResolverService: ValueResolverService,
        formFlowService: FormFlowService,
        processDocumentService: ProcessDocumentService,
        documentService: DocumentService

    ): ValtimoFormFlow {
        return ValtimoFormFlow(
            taskService,
            objectMapper,
            valueResolverService,
            formFlowService,
            processDocumentService,
            documentService
        )
    }

    @Bean
    @ConditionalOnMissingBean(FormFlowProcessLinkMapper::class)
    fun formFlowProcessLinkMapper(
        objectMapper: ObjectMapper,
        formFlowService: FormFlowService,
    ): FormFlowProcessLinkMapper {
        return FormFlowProcessLinkMapper(
            objectMapper,
            formFlowService
        )
    }

    @Bean
    @Order(20)
    @ConditionalOnMissingBean(FormFlowSupportedProcessLinksHandler::class)
    fun formFlowSupportedProcessLinks(formFlowService: FormFlowService): FormFlowSupportedProcessLinksHandler {
        return FormFlowSupportedProcessLinksHandler(formFlowService)
    }

    @Bean
    @ConditionalOnMissingBean(FormFlowStepCompletedEventListener::class)
    fun formFlowStepCompletedEventListener(
        outboxService: OutboxService,
        objectMapper: ObjectMapper
    ): FormFlowStepCompletedEventListener {
        return FormFlowStepCompletedEventListener(
            outboxService,
            objectMapper
        )
    }


    @Bean
    @ConditionalOnMissingBean(FormFlowDefinitionExporter::class)
    fun formFlowDefinitionExporter(
        objectMapper: ObjectMapper,
        formFlowService: FormFlowService
    ): FormFlowDefinitionExporter {
        return FormFlowDefinitionExporter(
            objectMapper,
            formFlowService
        )
    }

    @Bean
    @ConditionalOnMissingBean(FormFlowDefinitionImporter::class)
    fun formFlowDefinitionImporter(
        formFlowDeploymentService: FormFlowDeploymentService
    ): FormFlowDefinitionImporter {
        return FormFlowDefinitionImporter(
            formFlowDeploymentService
        )
    }

    @Bean
    @ConditionalOnMissingBean(FormFlowValtimoService::class)
    fun formFlowValtimoService(
        formDefinitionService: FormIoFormDefinitionService,
        objectMapper: ObjectMapper,
        @Value("\${valtimo.formFlow.doSubmissionDataFiltering:true}") doSubmissionDataFiltering: Boolean
    ): FormFlowValtimoService {
        return FormFlowValtimoService(
            formDefinitionService,
            objectMapper,
            doSubmissionDataFiltering
        )
    }
}
