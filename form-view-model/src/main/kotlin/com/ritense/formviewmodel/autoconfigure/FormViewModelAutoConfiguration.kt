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

package com.ritense.formviewmodel.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationService
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.form.service.FormDefinitionService
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formviewmodel.commandhandling.handler.CompleteTaskCommandHandler
import com.ritense.formviewmodel.commandhandling.handler.StartProcessCommandHandler
import com.ritense.formviewmodel.processlink.FormViewModelProcessLinkActivityHandler
import com.ritense.formviewmodel.security.config.FormViewModelHttpSecurityConfigurerKotlin
import com.ritense.formviewmodel.service.FormViewModelService
import com.ritense.formviewmodel.service.FormViewModelSubmissionService
import com.ritense.formviewmodel.service.ProcessAuthorizationService
import com.ritense.formviewmodel.submission.FormViewModelStartFormSubmissionHandler
import com.ritense.formviewmodel.submission.FormViewModelStartFormSubmissionHandlerFactory
import com.ritense.formviewmodel.submission.FormViewModelUserTaskSubmissionHandler
import com.ritense.formviewmodel.submission.FormViewModelUserTaskSubmissionHandlerFactory
import com.ritense.formviewmodel.validation.OnStartUpViewModelValidator
import com.ritense.formviewmodel.viewmodel.ViewModelLoader
import com.ritense.formviewmodel.viewmodel.ViewModelLoaderFactory
import com.ritense.formviewmodel.web.rest.FormViewModelResource
import com.ritense.formviewmodel.web.rest.error.FormViewModelModuleExceptionTranslator
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.service.CamundaProcessService
import com.ritense.valtimo.service.CamundaTaskService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

@AutoConfiguration
class FormViewModelAutoConfiguration {

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Bean
    fun formViewModelModuleExceptionTranslator() = FormViewModelModuleExceptionTranslator()

    @Bean
    fun formViewModelService(
        objectMapper: ObjectMapper,
        viewModelLoaderFactory: ViewModelLoaderFactory,
        camundaTaskService: CamundaTaskService,
        authorizationService: AuthorizationService,
        processAuthorizationService: ProcessAuthorizationService,
        processService: CamundaProcessService,
        processLinkService: ProcessLinkService,
        documentService: JsonSchemaDocumentService
    ) = FormViewModelService(
        objectMapper = objectMapper,
        viewModelLoaderFactory = viewModelLoaderFactory,
        camundaTaskService = camundaTaskService,
        authorizationService = authorizationService,
        processAuthorizationService = processAuthorizationService,
        processLinkService = processLinkService,
        documentService = documentService
    )

    @Bean
    fun formViewModelStartFormSubmissionHandlerFactory(
        formViewModelStartFormSubmissionHandlers: List<FormViewModelStartFormSubmissionHandler<*>>,
        formDefinitionService: FormDefinitionService
    ) = FormViewModelStartFormSubmissionHandlerFactory(
        handlers = formViewModelStartFormSubmissionHandlers,
        formDefinitionService = formDefinitionService
    )

    @Bean
    fun formViewModelUserTaskSubmissionHandlerFactory(
        formViewModelUserTaskSubmissionHandlers: List<FormViewModelUserTaskSubmissionHandler<*>>,
        formDefinitionService: FormDefinitionService
    ) = FormViewModelUserTaskSubmissionHandlerFactory(
        handlers = formViewModelUserTaskSubmissionHandlers,
        formDefinitionService = formDefinitionService
    )

    @Bean
    fun formViewModelSubmissionService(
        formViewModelStartFormSubmissionHandlerFactory: FormViewModelStartFormSubmissionHandlerFactory,
        formViewModelUserTaskSubmissionHandlerFactory: FormViewModelUserTaskSubmissionHandlerFactory,
        authorizationService: AuthorizationService,
        camundaTaskService: CamundaTaskService,
        objectMapper: ObjectMapper,
        processAuthorizationService: ProcessAuthorizationService,
        processLinkService: ProcessLinkService,
        documentService: JsonSchemaDocumentService
    ) = FormViewModelSubmissionService(
        formViewModelStartFormSubmissionHandlerFactory = formViewModelStartFormSubmissionHandlerFactory,
        formViewModelUserTaskSubmissionHandlerFactory = formViewModelUserTaskSubmissionHandlerFactory,
        authorizationService = authorizationService,
        camundaTaskService = camundaTaskService,
        objectMapper = objectMapper,
        processAuthorizationService = processAuthorizationService,
        processLinkService = processLinkService,
        documentService = documentService
    )

    @Order(390)
    @Bean
    fun formViewModelHttpSecurityConfigurerKotlin() = FormViewModelHttpSecurityConfigurerKotlin()

    @Bean
    fun formViewModelRestResource(
        formViewModelService: FormViewModelService,
        formViewModelSubmissionService: FormViewModelSubmissionService
    ) = FormViewModelResource(
        formViewModelService = formViewModelService,
        formViewModelSubmissionService = formViewModelSubmissionService
    )

    @Bean
    fun formViewModelProcessLinkTaskProvider(
        formDefinitionService: FormIoFormDefinitionService
    ) = FormViewModelProcessLinkActivityHandler(
        formDefinitionService = formDefinitionService
    )

    @Bean
    fun viewModelLoaderFactory(
        loaders: List<ViewModelLoader<*>>,
        formDefinitionService: FormDefinitionService
    ) = ViewModelLoaderFactory(
        viewModelLoaders = loaders,
        formDefinitionService = formDefinitionService
    )

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    fun onStartUpViewModelValidator(
        formIoFormDefinitionService: FormIoFormDefinitionService,
        viewModelLoaders: List<ViewModelLoader<*>>,
        formViewModelStartFormSubmissionHandlerFactory: FormViewModelStartFormSubmissionHandlerFactory,
        formViewModelUserTaskSubmissionHandlerFactory: FormViewModelUserTaskSubmissionHandlerFactory
    ) = OnStartUpViewModelValidator(
        formIoFormDefinitionService = formIoFormDefinitionService,
        viewModelLoaders = viewModelLoaders,
        formViewModelStartFormSubmissionHandlerFactory = formViewModelStartFormSubmissionHandlerFactory,
        formViewModelUserTaskSubmissionHandlerFactory = formViewModelUserTaskSubmissionHandlerFactory
    )

    @Bean
    fun processAuthorizationService(
        camundaRepositoryService: CamundaRepositoryService,
        authorizationService: AuthorizationService,
        documentService: JsonSchemaDocumentService
    ) = ProcessAuthorizationService(
        camundaRepositoryService = camundaRepositoryService,
        authorizationService = authorizationService,
    )

    @Bean
    @ConditionalOnMissingBean(name = ["startProcessCommandHandler"])
    fun startProcessCommandHandler(
        camundaProcessService: CamundaProcessService,
        processDocumentAssociationService: ProcessDocumentAssociationService
    ) = StartProcessCommandHandler(
        camundaProcessService = camundaProcessService,
        processDocumentAssociationService = processDocumentAssociationService
    )

    @Bean
    @ConditionalOnMissingBean(name = ["completeTaskCommandHandler"])
    fun completeTaskCommandHandler(
        camundaTaskService: CamundaTaskService
    ) = CompleteTaskCommandHandler(
        camundaTaskService = camundaTaskService
    )
}