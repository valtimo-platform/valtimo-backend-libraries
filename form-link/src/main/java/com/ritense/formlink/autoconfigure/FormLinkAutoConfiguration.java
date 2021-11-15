/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.formlink.autoconfigure;

import com.ritense.document.service.DocumentService;
import com.ritense.form.domain.FormIoFormDefinition;
import com.ritense.form.service.FormDefinitionService;
import com.ritense.formlink.autodeployment.FormLinkDeploymentService;
import com.ritense.formlink.autodeployment.FormsAutoDeploymentFinishedEventListener;
import com.ritense.formlink.repository.ProcessFormAssociationRepository;
import com.ritense.formlink.service.FormAssociationService;
import com.ritense.formlink.service.FormAssociationSubmissionService;
import com.ritense.formlink.service.SubmissionTransformerService;
import com.ritense.formlink.service.impl.CamundaFormAssociationService;
import com.ritense.formlink.service.impl.CamundaFormAssociationSubmissionService;
import com.ritense.formlink.service.impl.FormIoJsonPatchSubmissionTransformerService;
import com.ritense.formlink.web.rest.FormAssociationManagementResource;
import com.ritense.formlink.web.rest.FormAssociationResource;
import com.ritense.formlink.web.rest.impl.CamundaFormAssociationManagementResource;
import com.ritense.formlink.web.rest.impl.CamundaFormAssociationResource;
import com.ritense.formlink.web.rest.impl.interceptor.PublicAccessRateLimitInterceptor;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.processdocument.service.ProcessDocumentService;
import com.ritense.valtimo.contract.form.FormFieldDataResolver;
import com.ritense.valtimo.service.CamundaProcessService;
import com.ritense.valtimo.service.CamundaTaskService;
import org.camunda.bpm.engine.TaskService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

@Configuration
@EnableJpaRepositories(basePackages = "com.ritense.formlink.repository")
@EntityScan("com.ritense.formlink.domain")
public class FormLinkAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(FormAssociationService.class)
    public CamundaFormAssociationService formAssociationService(
        FormDefinitionService formDefinitionService,
        ProcessFormAssociationRepository processFormAssociationRepository,
        DocumentService documentService,
        ProcessDocumentAssociationService processDocumentAssociationService,
        CamundaProcessService camundaProcessService,
        TaskService taskService,
        SubmissionTransformerService<FormIoFormDefinition> submissionTransformerService,
        List<FormFieldDataResolver> formFieldDataResolvers
    ) {
        return new CamundaFormAssociationService(
            formDefinitionService,
            processFormAssociationRepository,
            documentService,
            processDocumentAssociationService,
            camundaProcessService,
            taskService,
            submissionTransformerService,
            formFieldDataResolvers
        );
    }

    @Bean
    @ConditionalOnMissingBean(FormAssociationSubmissionService.class)
    public CamundaFormAssociationSubmissionService formAssociationSubmissionService(
        FormDefinitionService formDefinitionService,
        DocumentService documentService,
        ProcessDocumentAssociationService processDocumentAssociationService,
        FormAssociationService formAssociationService,
        ProcessDocumentService processDocumentService,
        CamundaTaskService camundaTaskService,
        SubmissionTransformerService<FormIoFormDefinition> submissionTransformerService,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        return new CamundaFormAssociationSubmissionService(
            formDefinitionService,
            documentService,
            processDocumentAssociationService,
            formAssociationService,
            processDocumentService,
            camundaTaskService,
            submissionTransformerService,
            applicationEventPublisher
        );
    }

    @Bean
    @ConditionalOnMissingBean(SubmissionTransformerService.class)
    public FormIoJsonPatchSubmissionTransformerService submissionTransformerService() {
        return new FormIoJsonPatchSubmissionTransformerService();
    }

    @Bean
    @ConditionalOnMissingBean(FormAssociationResource.class)
    public CamundaFormAssociationResource formAssociationResource(
        FormAssociationService formAssociationService,
        FormAssociationSubmissionService formAssociationSubmissionService
    ) {
        return new CamundaFormAssociationResource(formAssociationService, formAssociationSubmissionService);
    }

    @Bean
    @ConditionalOnMissingBean(FormAssociationManagementResource.class)
    public CamundaFormAssociationManagementResource formAssociationManagementResource(FormAssociationService formAssociationService) {
        return new CamundaFormAssociationManagementResource(formAssociationService);
    }

    @Bean
    @ConditionalOnMissingBean(PublicAccessRateLimitInterceptor.class)
    public PublicAccessRateLimitInterceptor publicAccessRateLimitInterceptor() {
        return new PublicAccessRateLimitInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean(FormLinkDeploymentService.class)
    public FormLinkDeploymentService formLinkDeploymentService(
        ResourceLoader resourceLoader,
        FormAssociationService formAssociationService,
        FormDefinitionService formDefinitionService
    ) {
        return new FormLinkDeploymentService(resourceLoader, formAssociationService, formDefinitionService);
    }

    @Bean
    @ConditionalOnMissingBean(FormsAutoDeploymentFinishedEventListener.class)
    public FormsAutoDeploymentFinishedEventListener formsAutoDeploymentFinishedEventListener(
        FormLinkDeploymentService formLinkDeploymentService
    ) {
        return new FormsAutoDeploymentFinishedEventListener(formLinkDeploymentService);
    }

}