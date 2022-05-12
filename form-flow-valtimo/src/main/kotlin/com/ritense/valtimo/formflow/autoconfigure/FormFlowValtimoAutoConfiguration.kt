/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

import com.ritense.document.service.DocumentService
import com.ritense.document.service.DocumentService
import com.ritense.form.service.FormDefinitionService
import com.ritense.form.service.FormLoaderService
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formflow.service.FormFlowObjectMapper
import com.ritense.formflow.service.FormFlowService
import com.ritense.formlink.domain.ProcessLinkTaskProvider
import com.ritense.formlink.service.FormAssociationService
import com.ritense.valtimo.formflow.FormFlowProcessLinkTaskProvider
import com.ritense.valtimo.formflow.FormFlowTaskOpenResultProperties
import com.ritense.valtimo.formflow.ValtimoFormFlowHttpSecurityConfigurer
import com.ritense.valtimo.formflow.handler.FormFlowCreateTaskEventHandler
import com.ritense.formlink.service.impl.CamundaFormAssociationService
import com.ritense.valtimo.formflow.handler.FormFlowStepTypeFormHandler
import com.ritense.valtimo.formflow.security.ValtimoFormFlowHttpSecurityConfigurer
import com.ritense.valtimo.formflow.web.rest.FormFlowDemoResource
import com.ritense.valtimo.formflow.web.rest.FormFlowResource
import com.ritense.valtimo.formflow.web.rest.ProcessLinkFormFlowDefinitionResource
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order

@Configuration
class FormFlowValtimoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(FormFlowCreateTaskEventHandler::class)
    fun formFlowCreateTaskCommandHandler(formFlowService: FormFlowService,
        formAssociationService: FormAssociationService,
        documentService: DocumentService
    ): FormFlowCreateTaskEventHandler {
        return FormFlowCreateTaskEventHandler(formFlowService, formAssociationService, documentService)
    }

    @Bean
    fun formFlowProcessLinkTaskProvider(
        formFlowService: FormFlowService
    ): ProcessLinkTaskProvider<FormFlowTaskOpenResultProperties> {
        return FormFlowProcessLinkTaskProvider(formFlowService)
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
        formDefinitionService: FormDefinitionService
    ): FormFlowResource {
        return FormFlowResource(formFlowService, formDefinitionService)
    }

    @Bean
    @ConditionalOnMissingBean(FormFlowDemoResource::class)
    fun formFlowDemoResource(
        formFlowService: FormFlowService,
        formLoaderService: FormLoaderService
    ): FormFlowDemoResource {
        return FormFlowDemoResource(formFlowService, formLoaderService)
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
        objectMapper: FormFlowObjectMapper
    ): FormFlowStepTypeFormHandler {
        return FormFlowStepTypeFormHandler(
            formIoFormDefinitionService,
            camundaFormAssociationService,
            documentService,
            objectMapper
        )
    }
}
