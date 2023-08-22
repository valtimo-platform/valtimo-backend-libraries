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

package com.ritense.valtimo.formflow.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.document.service.DocumentService
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formflow.service.FormFlowService
import com.ritense.formlink.autoconfigure.FormLinkAutoConfiguration
import com.ritense.formlink.domain.FormLinkTaskProvider
import com.ritense.formlink.repository.ProcessFormAssociationRepository
import com.ritense.formlink.service.FormAssociationService
import com.ritense.formlink.service.FormLinkNewProcessFormFlowProvider
import com.ritense.formlink.service.impl.CamundaFormAssociationService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processlink.service.ProcessLinkActivityHandler
import com.ritense.valtimo.formflow.FormFlowFormLinkTaskProvider
import com.ritense.valtimo.formflow.FormFlowProcessLinkActivityHandler
import com.ritense.tenancy.TenantResolver
import com.ritense.valtimo.formflow.FormFlowProcessLinkTaskProvider
import com.ritense.valtimo.formflow.FormFlowTaskOpenResultProperties
import com.ritense.valtimo.formflow.FormLinkNewProcessFormFlowProviderImpl
import com.ritense.valtimo.formflow.common.ValtimoFormFlow
import com.ritense.valtimo.formflow.service.FormFlowSupportedProcessLinksHandler
import com.ritense.valtimo.formflow.handler.FormFlowStepTypeFormHandler
import com.ritense.valtimo.formflow.mapper.FormFlowProcessLinkMapper
import com.ritense.valtimo.formflow.repository.FormFlowProcessLinkRepository
import com.ritense.valtimo.formflow.security.ValtimoFormFlowHttpSecurityConfigurer
import com.ritense.valtimo.formflow.web.rest.FormFlowResource
import com.ritense.valtimo.formflow.web.rest.ProcessLinkFormFlowDefinitionResource
import com.ritense.valueresolver.ValueResolverService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@AutoConfigureBefore(FormLinkAutoConfiguration::class)
@EnableJpaRepositories(
    basePackageClasses = [FormFlowProcessLinkRepository::class]
)
@EntityScan(basePackages = ["com.ritense.valtimo.formflow.domain"])
class FormFlowValtimoAutoConfiguration {

    @Bean
    fun formFlowFormLinkTaskProvider(
        formFlowService: FormFlowService,
        formAssociationService: FormAssociationService,
        documentService: DocumentService,
        repositoryService: RepositoryService,
        runtimeService: RuntimeService,
    ): FormLinkTaskProvider<FormFlowTaskOpenResultProperties> {
        return FormFlowFormLinkTaskProvider(
            formFlowService,
            formAssociationService,
            documentService,
            repositoryService,
            runtimeService,
            tenantResolver
        )
    }

    @Bean
    fun formFlowProcessLinkTaskProvider(
        formFlowService: FormFlowService,
        repositoryService: RepositoryService,
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
    fun formLinkNewProcessFormFlowProvider(
        formFlowService: FormFlowService,
        processFormAssociationRepository: ProcessFormAssociationRepository
    ): FormLinkNewProcessFormFlowProvider {
        return FormLinkNewProcessFormFlowProviderImpl(
            formFlowService,
            processFormAssociationRepository
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
        formFlowService: FormFlowService
    ): FormFlowResource {
        return FormFlowResource(formFlowService)
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
        camundaFormAssociationService: CamundaFormAssociationService,
        documentService: DocumentService,
        objectMapper: ObjectMapper,
        tenantResolver: TenantResolver
    ): FormFlowStepTypeFormHandler {
        return FormFlowStepTypeFormHandler(
            formIoFormDefinitionService,
            camundaFormAssociationService,
            documentService,
            objectMapper,
            tenantResolver
        )
    }

    @Bean
    @ConditionalOnMissingBean(ValtimoFormFlow::class)
    fun valtimoFormFlow(
        taskService: TaskService,
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
    @ConditionalOnMissingBean(FormFlowSupportedProcessLinksHandler::class)
    fun formFlowSupportedProcessLinks(formFlowService: FormFlowService): FormFlowSupportedProcessLinksHandler {
        return FormFlowSupportedProcessLinksHandler(formFlowService)
    }
}
