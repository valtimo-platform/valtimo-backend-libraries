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

package com.ritense.formlink.autoconfigure;

import com.ritense.authorization.AuthorizationService;
import com.ritense.document.service.DocumentService;
import com.ritense.form.domain.FormIoFormDefinition;
import com.ritense.form.service.FormDefinitionService;
import com.ritense.formlink.autodeployment.FormLinkDeploymentService;
import com.ritense.formlink.autodeployment.FormsAutoDeploymentFinishedEventListener;
import com.ritense.formlink.domain.FormLinkTaskProvider;
import com.ritense.formlink.domain.impl.formassociation.FormFormLinkTaskProvider;
import com.ritense.formlink.repository.ProcessFormAssociationRepository;
import com.ritense.formlink.repository.impl.JdbcProcessFormAssociationRepository;
import com.ritense.formlink.service.FormAssociationService;
import com.ritense.formlink.service.FormAssociationSubmissionService;
import com.ritense.formlink.service.FormLinkNewProcessFormFlowProvider;
import com.ritense.formlink.service.ProcessLinkService;
import com.ritense.formlink.service.SubmissionTransformerService;
import com.ritense.formlink.service.impl.CamundaFormAssociationService;
import com.ritense.formlink.service.impl.CamundaFormAssociationSubmissionService;
import com.ritense.formlink.service.impl.DefaultProcessLinkService;
import com.ritense.formlink.service.impl.FormIoJsonPatchSubmissionTransformerService;
import com.ritense.formlink.web.rest.FormAssociationManagementResource;
import com.ritense.formlink.web.rest.FormAssociationResource;
import com.ritense.formlink.web.rest.FormLinkFormFlowResource;
import com.ritense.formlink.web.rest.ProcessLinkResource;
import com.ritense.formlink.web.rest.impl.CamundaFormAssociationManagementResource;
import com.ritense.formlink.web.rest.impl.CamundaFormAssociationResource;
import com.ritense.formlink.web.rest.impl.DefaultProcessLinkResource;
import com.ritense.formlink.web.rest.impl.interceptor.PublicAccessRateLimitInterceptor;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.processdocument.service.ProcessDocumentService;
import com.ritense.valtimo.camunda.service.CamundaRepositoryService;
import com.ritense.valtimo.camunda.service.CamundaRuntimeService;
import com.ritense.valtimo.contract.form.FormFieldDataResolver;
import com.ritense.valtimo.service.CamundaTaskService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

@Deprecated(since = "10.6.0", forRemoval = true)
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
        CamundaRuntimeService runtimeService,
        CamundaTaskService taskService,
        SubmissionTransformerService<FormIoFormDefinition> submissionTransformerService,
        List<FormFieldDataResolver> formFieldDataResolvers
    ) {
        return new CamundaFormAssociationService(
            formDefinitionService,
            processFormAssociationRepository,
            documentService,
            processDocumentAssociationService,
            runtimeService,
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
        ApplicationEventPublisher applicationEventPublisher,
        AuthorizationService authorizationService
    ) {
        return new CamundaFormAssociationSubmissionService(
            formDefinitionService,
            documentService,
            processDocumentAssociationService,
            formAssociationService,
            processDocumentService,
            camundaTaskService,
            submissionTransformerService,
            applicationEventPublisher,
            authorizationService);
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

    @Bean("formProcessLinkResource")
    @ConditionalOnMissingBean(ProcessLinkResource.class)
    public ProcessLinkResource defaultProcessLinkResource(
        ProcessLinkService processLinkService
    ) {
        return new DefaultProcessLinkResource(processLinkService);
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

    @Bean
    public FormLinkTaskProvider formFormLinkTaskProvider() {
        return new FormFormLinkTaskProvider();
    }

    @Bean("formProcessLinkService")
    @ConditionalOnMissingBean(ProcessLinkService.class)
    public ProcessLinkService processLinkService(
        CamundaRepositoryService repositoryService,
        CamundaTaskService taskService,
        FormAssociationService formAssociationService,
        List<FormLinkTaskProvider> processLinkTaskProvide
    ) {
        return new DefaultProcessLinkService(repositoryService, taskService, formAssociationService, processLinkTaskProvide);
    }

    @Bean
    @ConditionalOnMissingBean(ProcessFormAssociationRepository.class)
    public JdbcProcessFormAssociationRepository processFormAssociationRepository(
        final NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        return new JdbcProcessFormAssociationRepository(namedParameterJdbcTemplate);
    }

    @Bean
    @ConditionalOnBean(FormLinkNewProcessFormFlowProvider.class)
    @ConditionalOnMissingBean(FormLinkFormFlowResource.class)
    public FormLinkFormFlowResource processLinkFormFlowResource(
        FormLinkNewProcessFormFlowProvider formLinkNewProcessFormFlowProvider
    ) {
        return new FormLinkFormFlowResource(formLinkNewProcessFormFlowProvider);
    }

}
