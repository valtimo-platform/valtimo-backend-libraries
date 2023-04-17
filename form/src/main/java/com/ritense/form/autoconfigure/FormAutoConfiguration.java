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

package com.ritense.form.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.document.service.DocumentService;
import com.ritense.form.autodeployment.FormApplicationReadyEventListener;
import com.ritense.form.autodeployment.FormDefinitionDeploymentService;
import com.ritense.form.domain.FormSpringContextHelper;
import com.ritense.form.mapper.FormProcessLinkMapper;
import com.ritense.form.processlink.FormProcessLinkTaskProvider;
import com.ritense.form.repository.FormDefinitionRepository;
import com.ritense.form.service.FormDefinitionService;
import com.ritense.form.service.FormLoaderService;
import com.ritense.form.service.PrefillFormService;
import com.ritense.form.service.impl.FormIoFormDefinitionService;
import com.ritense.form.service.impl.FormIoFormLoaderService;
import com.ritense.form.web.rest.FormFileResource;
import com.ritense.form.web.rest.FormManagementResource;
import com.ritense.form.web.rest.impl.FormIoFormFileResource;
import com.ritense.form.web.rest.impl.FormIoFormManagementResource;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.resource.service.ResourceService;
import com.ritense.valtimo.contract.form.FormFieldDataResolver;
import com.ritense.valtimo.service.CamundaProcessService;
import org.camunda.bpm.engine.TaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

@Configuration
@EnableJpaRepositories(basePackages = "com.ritense.form.repository")
@EntityScan("com.ritense.form.domain")
public class FormAutoConfiguration {
    private static boolean ignoreDisabledFields = false;

    public FormAutoConfiguration(@Value("${valtimo.form.ignoreDisabledFields:false}") boolean ignoreDisabledFields) {
        FormAutoConfiguration.ignoreDisabledFields = ignoreDisabledFields;
    }

    public static boolean isIgnoreDisabledFields() {
        return ignoreDisabledFields;
    }

    @Bean
    @ConditionalOnMissingBean(FormLoaderService.class)
    public FormIoFormLoaderService formLoaderService(
        DocumentService documentService,
        FormDefinitionRepository formDefinitionRepository
    ) {
        return new FormIoFormLoaderService(documentService, formDefinitionRepository);
    }

    @Bean
    @ConditionalOnMissingBean(FormDefinitionService.class)
    public FormIoFormDefinitionService formDefinitionService(final FormDefinitionRepository formDefinitionRepository) {
        return new FormIoFormDefinitionService(formDefinitionRepository);
    }

    @Bean
    @ConditionalOnMissingBean(FormDefinitionDeploymentService.class)
    public FormDefinitionDeploymentService formDefinitionDeploymentService(
        ResourceLoader resourceLoader,
        FormDefinitionService formDefinitionService,
        FormDefinitionRepository formDefinitionRepository,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        return new FormDefinitionDeploymentService(resourceLoader, formDefinitionService, formDefinitionRepository, applicationEventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean(FormApplicationReadyEventListener.class)
    public FormApplicationReadyEventListener formApplicationReadyEventListener(FormDefinitionDeploymentService formDefinitionDeploymentService) {
        return new FormApplicationReadyEventListener(formDefinitionDeploymentService);
    }

    @Bean
    @ConditionalOnMissingBean(FormFileResource.class)
    public FormIoFormFileResource formFileResource(ResourceService resourceService) {
        return new FormIoFormFileResource(resourceService);
    }

    @Bean
    @ConditionalOnMissingBean(FormManagementResource.class)
    public FormIoFormManagementResource formManagementResource(FormDefinitionService formDefinitionService) {
        return new FormIoFormManagementResource(formDefinitionService);
    }

    @Bean("formSpringContextHelper")
    @ConditionalOnMissingBean(FormSpringContextHelper.class)
    public FormSpringContextHelper formSpringContextHelper() {
        return new FormSpringContextHelper();
    }

    @Bean
    @ConditionalOnMissingBean(FormProcessLinkMapper.class)
    public FormProcessLinkMapper formProcessLinkMapper(
        final ObjectMapper objectMapper,
        final FormDefinitionService formDefinitionService
    ) {
        return new FormProcessLinkMapper(objectMapper, formDefinitionService);
    }

    @Bean
    @ConditionalOnMissingBean(PrefillFormService.class)
    public PrefillFormService prefillFormService(
        DocumentService documentService,
        FormIoFormDefinitionService formDefinitionService,
        CamundaProcessService camundaProcessService,
        TaskService taskService,
        List<FormFieldDataResolver> formFieldDataResolvers,
        ProcessDocumentAssociationService processDocumentAssociationService
    ) {
        return new PrefillFormService(
            documentService,
            formDefinitionService,
            camundaProcessService,
            taskService,
            formFieldDataResolvers,
            processDocumentAssociationService
        );
    }

    @Bean
    @ConditionalOnMissingBean(FormProcessLinkTaskProvider.class)
    public FormProcessLinkTaskProvider formProcessLinkTaskProvider(
        PrefillFormService prefillFormService
    ) {
        return new FormProcessLinkTaskProvider(prefillFormService);
    }
}
