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

package com.ritense.processdocument.autoconfigure

import com.ritense.case.service.CaseDefinitionService
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.camunda.authorization.CamundaTaskDocumentMapper
import com.ritense.processdocument.domain.impl.delegate.DocumentDelegate
import com.ritense.processdocument.listener.CaseAssigneeListener
import com.ritense.processdocument.listener.CaseAssigneeTaskCreatedListener
import com.ritense.processdocument.service.CorrelationService
import com.ritense.processdocument.service.CorrelationServiceImpl
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processdocument.service.ProcessDocumentsService
import com.ritense.processdocument.service.impl.CamundaProcessJsonSchemaDocumentService
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.camunda.service.CamundaRuntimeService
import com.ritense.valtimo.contract.annotation.ProcessBean
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.service.CamundaProcessService
import com.ritense.valtimo.service.CamundaTaskService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
class ProcessDocumentsAutoConfiguration {

    @ProcessBean
    @Bean
    @ConditionalOnMissingBean(DocumentDelegate::class)
    fun documentDelegate(
        processDocumentService: ProcessDocumentService,
        userManagementService: UserManagementService,
        documentService: DocumentService,
    ): DocumentDelegate {
        return DocumentDelegate(
            processDocumentService,
            userManagementService,
            documentService,
        )
    }

    @ProcessBean
    @Bean
    @ConditionalOnMissingBean(CorrelationService::class)
    fun correlationService(
        runtimeService: RuntimeService,
        camundaRuntimeService: CamundaRuntimeService,
        documentService: DocumentService,
        processDocumentAssociationService: ProcessDocumentAssociationService,
        camundaProcessService: CamundaProcessService,
        repositoryService: RepositoryService,
        camundaRepositoryService: CamundaRepositoryService,
    ): CorrelationService {
        return CorrelationServiceImpl(
            runtimeService = runtimeService,
            camundaRuntimeService = camundaRuntimeService,
            documentService = documentService,
            camundaRepositoryService = camundaRepositoryService,
            repositoryService = repositoryService,
            associationService = processDocumentAssociationService
        )
    }

    @ProcessBean
    @Bean("processService")
    @ConditionalOnMissingBean(ProcessDocumentsService::class)
    fun processDocumentsService(
        documentService: DocumentService,
        processDocumentAssociationService: ProcessDocumentAssociationService,
        camundaProcessService: CamundaProcessService
    ): ProcessDocumentsService {
        return ProcessDocumentsService(
            documentService,
            camundaProcessService,
            processDocumentAssociationService
        )
    }

    @Bean
    fun caseAssigneeCamundaTaskListener(
        taskService: TaskService,
        documentService: DocumentService,
        caseDefinitionService: CaseDefinitionService,
        userManagementService: UserManagementService
    ): CaseAssigneeTaskCreatedListener {
        return CaseAssigneeTaskCreatedListener(
            taskService, documentService, caseDefinitionService, userManagementService
        )
    }

    @Bean
    fun caseAssigneeListener(
        camundaTaskService: CamundaTaskService,
        documentService: DocumentService,
        caseDefinitionService: CaseDefinitionService,
        userManagementService: UserManagementService
    ): CaseAssigneeListener {
        return CaseAssigneeListener(
            camundaTaskService, documentService, caseDefinitionService, userManagementService
        )
    }

    @Bean
    @ConditionalOnMissingBean(CamundaTaskDocumentMapper::class)
    fun camundaTaskDocumentMapper(
        @Lazy processDocumentService: CamundaProcessJsonSchemaDocumentService
    ): CamundaTaskDocumentMapper {
        return CamundaTaskDocumentMapper(processDocumentService)
    }
}
