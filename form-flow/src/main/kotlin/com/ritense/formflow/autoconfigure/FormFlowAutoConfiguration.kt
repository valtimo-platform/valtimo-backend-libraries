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

package com.ritense.formflow.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.ritense.document.service.DocumentService
import com.ritense.form.service.PrefillFormService
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formflow.FormFlowProcessLinkActivityHandler
import com.ritense.formflow.FormFlowTaskOpenResultProperties
import com.ritense.formflow.common.ValtimoFormFlow
import com.ritense.formflow.domain.definition.configuration.step.CustomComponentStepTypeProperties
import com.ritense.formflow.domain.definition.configuration.step.FormStepTypeProperties
import com.ritense.formflow.event.ApplicationEventPublisherHolder
import com.ritense.formflow.event.FormFlowStepCompletedEventListener
import com.ritense.formflow.exporter.FormFlowDefinitionExporter
import com.ritense.formflow.handler.ApplicationReadyEventHandler
import com.ritense.formflow.handler.FormFlowStepTypeCustomComponentHandler
import com.ritense.formflow.handler.FormFlowStepTypeFormHandler
import com.ritense.formflow.handler.FormFlowStepTypeHandler
import com.ritense.formflow.importer.FormFlowDefinitionImporter
import com.ritense.formflow.json.MapperSingleton
import com.ritense.formflow.listener.FormFlowCaseEventListener
import com.ritense.formflow.mapper.FormFlowProcessLinkMapper
import com.ritense.formflow.repository.FormFlowAdditionalPropertiesSearchRepository
import com.ritense.formflow.repository.FormFlowDefinitionRepository
import com.ritense.formflow.repository.FormFlowInstanceRepository
import com.ritense.formflow.repository.FormFlowStepInstanceRepository
import com.ritense.formflow.repository.FormFlowStepRepository
import com.ritense.formflow.repository.MySqlFormFlowAdditionalPropertiesSearchRepository
import com.ritense.formflow.repository.PostgresFormFlowAdditionalPropertiesSearchRepository
import com.ritense.formflow.security.ValtimoFormFlowHttpSecurityConfigurer
import com.ritense.formflow.service.FormFlowService
import com.ritense.formflow.service.FormFlowSupportedProcessLinksHandler
import com.ritense.formflow.service.FormFlowValtimoService
import com.ritense.formflow.service.ObjectMapperConfigurer
import com.ritense.formflow.web.rest.FormFlowManagementResource
import com.ritense.formflow.web.rest.FormFlowResource
import com.ritense.formflow.web.rest.ProcessLinkFormFlowDefinitionResource
import com.ritense.outbox.OutboxService
import com.ritense.processdocument.service.ProcessDefinitionCaseDefinitionService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processlink.service.ProcessLinkActivityHandler
import com.ritense.valtimo.operaton.service.OperatonRepositoryService
import com.ritense.valtimo.contract.case_.CaseDefinitionChecker
import com.ritense.valtimo.service.OperatonTaskService
import com.ritense.valueresolver.ValueResolverService
import jakarta.persistence.EntityManager
import org.operaton.bpm.engine.RuntimeService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order
import org.springframework.core.io.ResourceLoader
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@AutoConfiguration
@EnableJpaRepositories(
    basePackageClasses = [
        FormFlowDefinitionRepository::class,
        FormFlowStepRepository::class,
        FormFlowInstanceRepository::class,
        FormFlowStepInstanceRepository::class]
)
@EntityScan(basePackages = ["com.ritense.formflow.domain"])
class FormFlowAutoConfiguration {

    @Bean
    fun formStepPropertiesType(): NamedType {
        return NamedType(FormStepTypeProperties::class.java, "form")
    }

    @Bean
    fun customComponentStepPropertiesType(): NamedType {
        return NamedType(CustomComponentStepTypeProperties::class.java, "custom-component")
    }

    @Bean
    fun formFlowObjectMapper(
        objectMapper: ObjectMapper,
        stepPropertiesTypes: Collection<NamedType>
    ): ObjectMapperConfigurer {
        return ObjectMapperConfigurer(objectMapper, stepPropertiesTypes)
    }

    @Bean
    @ConditionalOnMissingBean(ApplicationReadyEventHandler::class)
    fun applicationReadyEventHandler(
        applicationContext: ApplicationContext
    ): ApplicationReadyEventHandler {
        return ApplicationReadyEventHandler(applicationContext)
    }

    @Bean
    @ConditionalOnMissingBean(FormFlowService::class)
    fun formFlowService(
        formFlowDefinitionRepository: FormFlowDefinitionRepository,
        formFlowInstanceRepository: FormFlowInstanceRepository,
        formFlowAdditionalPropertiesSearchRepository: FormFlowAdditionalPropertiesSearchRepository,
        formFlowStepTypeHandlers: List<FormFlowStepTypeHandler>,
        caseDefinitionChecker: CaseDefinitionChecker,
    ): FormFlowService {
        return FormFlowService(
            formFlowDefinitionRepository,
            formFlowInstanceRepository,
            formFlowAdditionalPropertiesSearchRepository,
            formFlowStepTypeHandlers,
            caseDefinitionChecker
        )
    }

    @Bean
    @ConditionalOnProperty(prefix = "valtimo", name = ["database"], havingValue = "postgres")
    fun postgresFormFlowAdditionalPropertiesSearchRepository(
        entityManager: EntityManager,
        objectMapper: ObjectMapper
    ): FormFlowAdditionalPropertiesSearchRepository {
        return PostgresFormFlowAdditionalPropertiesSearchRepository(entityManager, objectMapper)
    }

    @Bean
    @ConditionalOnProperty(prefix = "valtimo", name = ["database"], havingValue = "mysql", matchIfMissing = true)
    fun mysqlFormFlowAdditionalPropertiesSearchRepository(
        entityManager: EntityManager
    ): FormFlowAdditionalPropertiesSearchRepository {
        return MySqlFormFlowAdditionalPropertiesSearchRepository(entityManager)
    }

    @Bean
    @ConditionalOnMissingBean(ApplicationEventPublisherHolder::class)
    fun applicationEventPublisherHolder(
        applicationEventPublisher: ApplicationEventPublisher,
    ): ApplicationEventPublisherHolder {
        return ApplicationEventPublisherHolder(
            applicationEventPublisher
        )
    }

    @ConditionalOnMissingBean(name = ["mapperSingleton"])
    @Bean
    fun mapperSingleton(objectMapper: ObjectMapper): MapperSingleton {
        MapperSingleton.set(objectMapper)
        return MapperSingleton
    }

    @Bean
    fun formFlowProcessLinkTaskProvider(
        formFlowService: FormFlowService,
        repositoryService: OperatonRepositoryService,
        processDefinitionCaseDefinitionService: ProcessDefinitionCaseDefinitionService,
        documentService: DocumentService,
        runtimeService: RuntimeService,
    ): ProcessLinkActivityHandler<FormFlowTaskOpenResultProperties> {
        return FormFlowProcessLinkActivityHandler(
            formFlowService,
            repositoryService,
            processDefinitionCaseDefinitionService,
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
        formFlowDefinitionImporter: FormFlowDefinitionImporter
    ): FormFlowManagementResource {
        return FormFlowManagementResource(formFlowService, formFlowDefinitionImporter)
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
        taskService: OperatonTaskService,
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
        resourceLoader: ResourceLoader,
        formFlowService: FormFlowService,
        objectMapper: ObjectMapper
    ): FormFlowDefinitionImporter {
        return FormFlowDefinitionImporter(
            resourceLoader,
            formFlowService,
            objectMapper
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

    @Bean
    @ConditionalOnMissingBean(FormFlowCaseEventListener::class)
    fun formFlowCaseEventListener(service: FormFlowService): FormFlowCaseEventListener {
        return FormFlowCaseEventListener(service)
    }
}
