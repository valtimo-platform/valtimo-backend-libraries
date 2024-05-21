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
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandler
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandlerFactory
import com.ritense.formviewmodel.processlink.FormViewModelProcessLinkActivityHandler
import com.ritense.formviewmodel.security.config.FormViewModelHttpSecurityConfigurerKotlin
import com.ritense.formviewmodel.service.FormViewModelService
import com.ritense.formviewmodel.service.FormViewModelSubmissionService
import com.ritense.formviewmodel.validation.OnStartUpViewModelValidator
import com.ritense.formviewmodel.viewmodel.Submission
import com.ritense.formviewmodel.viewmodel.ViewModelLoader
import com.ritense.formviewmodel.viewmodel.ViewModelLoaderFactory
import com.ritense.formviewmodel.web.rest.FormViewModelResource
import com.ritense.formviewmodel.web.rest.error.FormViewModelModuleExceptionTranslator
import com.ritense.valtimo.service.CamundaTaskService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

@AutoConfiguration
class FormViewModelAutoConfiguration {

    @Bean
    fun formViewModelModuleExceptionTranslator() = FormViewModelModuleExceptionTranslator()

    @Bean
    fun formViewModelService(
        objectMapper: ObjectMapper
    ) = FormViewModelService(
        objectMapper
    )

    @Bean
    fun formViewModelSubmissionHandlerFactory(
        formViewModelSubmissionHandlers: List<FormViewModelSubmissionHandler<Submission>>,
    ) = FormViewModelSubmissionHandlerFactory(
        formViewModelSubmissionHandlers
    )

    @Bean
    fun formViewModelSubmissionService(
        formViewModelSubmissionHandlerFactory: FormViewModelSubmissionHandlerFactory,
        camundaTaskService: CamundaTaskService,
        objectMapper: ObjectMapper
    ) = FormViewModelSubmissionService(
        formViewModelSubmissionHandlerFactory,
        camundaTaskService,
        objectMapper
    )

    @Order(390)
    @Bean
    fun formViewModelHttpSecurityConfigurerKotlin() = FormViewModelHttpSecurityConfigurerKotlin()

    @Bean
    fun formViewModelRestResource(
        viewModelLoaderFactory: ViewModelLoaderFactory,
        camundaTaskService: CamundaTaskService,
        authorizationService: AuthorizationService,
        formViewModelService: FormViewModelService,
        formViewModelSubmissionService: FormViewModelSubmissionService
    ) = FormViewModelResource(
        viewModelLoaderFactory,
        camundaTaskService,
        authorizationService,
        formViewModelService,
        formViewModelSubmissionService
    )

    @Bean
    fun formViewModelProcessLinkTaskProvider(
        formDefinitionService: FormIoFormDefinitionService
    ): FormViewModelProcessLinkActivityHandler {
        return FormViewModelProcessLinkActivityHandler(formDefinitionService)
    }

    @Bean
    fun viewModelLoaderFactory(
        loaders: List<ViewModelLoader<*>>
    ) = ViewModelLoaderFactory(
        loaders
    )

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    fun onStartUpViewModelValidator(
        formIoFormDefinitionService: FormIoFormDefinitionService,
        viewModelLoaders: List<ViewModelLoader<*>>,
        formViewModelSubmissionHandlerFactory: FormViewModelSubmissionHandlerFactory
    ) = OnStartUpViewModelValidator(
        formIoFormDefinitionService,
        viewModelLoaders,
        formViewModelSubmissionHandlerFactory
    )
}