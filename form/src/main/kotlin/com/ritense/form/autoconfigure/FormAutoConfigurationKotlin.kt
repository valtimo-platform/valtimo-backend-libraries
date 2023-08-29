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

package com.ritense.form.autoconfigure

import com.ritense.authorization.AuthorizationService
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.form.security.config.FormHttpSecurityConfigurerKotlin
import com.ritense.form.service.FormDefinitionService
import com.ritense.form.service.FormSubmissionService
import com.ritense.form.service.FormSupportedProcessLinksHandler
import com.ritense.form.service.PrefillFormService
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.form.web.rest.FormResource
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.service.CamundaTaskService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order

@Configuration
class FormAutoConfigurationKotlin {

    @Bean
    @ConditionalOnMissingBean(FormSupportedProcessLinksHandler::class)
    fun formSupportedProcessLinks(formDefinitionService: FormDefinitionService): FormSupportedProcessLinksHandler {
        return FormSupportedProcessLinksHandler(formDefinitionService)
    }

    @Order(270)
    @Bean
    @ConditionalOnMissingBean(FormHttpSecurityConfigurerKotlin::class)
    fun formHttpSecurityConfigurerKotlin(): FormHttpSecurityConfigurerKotlin {
        return FormHttpSecurityConfigurerKotlin()
    }

    @Bean
    @ConditionalOnMissingBean(FormResource::class)
    fun formResource(
        formSubmissionService: FormSubmissionService,
        prefillFormService: PrefillFormService,
        formDefinitionService: FormDefinitionService,
    ): FormResource {
        return FormResource(
            formSubmissionService,
            prefillFormService,
            formDefinitionService
        )
    }

    @Bean
    @ConditionalOnMissingBean(FormSubmissionService::class)
    fun formSubmissionService(
        processLinkService: ProcessLinkService,
        formDefinitionService: FormIoFormDefinitionService,
        documentService: JsonSchemaDocumentService,
        processDocumentAssociationService: ProcessDocumentAssociationService,
        processDocumentService: ProcessDocumentService,
        camundaTaskService: CamundaTaskService,
        repositoryService: CamundaRepositoryService,
        applicationEventPublisher: ApplicationEventPublisher,
        prefillFormService: PrefillFormService,
        authorizationService: AuthorizationService,
    ): FormSubmissionService {
        return FormSubmissionService(
            processLinkService,
            formDefinitionService,
            documentService,
            processDocumentAssociationService,
            processDocumentService,
            camundaTaskService,
            repositoryService,
            applicationEventPublisher,
            prefillFormService,
            authorizationService
        )
    }

}
