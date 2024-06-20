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

package com.ritense.processdocument.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationService
import com.ritense.case.repository.TaskListColumnRepository
import com.ritense.case.service.CaseDefinitionService
import com.ritense.document.service.DocumentService
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.processdocument.camunda.authorization.CamundaTaskDocumentMapper
import com.ritense.processdocument.domain.impl.delegate.DocumentDelegate
import com.ritense.processdocument.exporter.ProcessDocumentLinkExporter
import com.ritense.processdocument.importer.ProcessDocumentLinkImporter
import com.ritense.processdocument.listener.CaseAssigneeListener
import com.ritense.processdocument.listener.CaseAssigneeTaskCreatedListener
import com.ritense.processdocument.service.CaseTaskListSearchService
import com.ritense.processdocument.service.CorrelationService
import com.ritense.processdocument.service.CorrelationServiceImpl
import com.ritense.processdocument.service.DocumentDelegateService
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentDeploymentService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processdocument.service.ProcessDocumentsService
import com.ritense.processdocument.service.ValueResolverDelegateService
import com.ritense.processdocument.service.impl.CamundaProcessJsonSchemaDocumentService
import com.ritense.processdocument.tasksearch.TaskListSearchFieldV2Mapper
import com.ritense.processdocument.web.TaskListResource
import com.ritense.search.service.SearchFieldV2Service
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.camunda.service.CamundaRuntimeService
import com.ritense.valtimo.contract.annotation.ProcessBean
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.database.QueryDialectHelper
import com.ritense.valtimo.service.CamundaProcessService
import com.ritense.valtimo.service.CamundaTaskService
import com.ritense.valueresolver.ValueResolverService
import jakarta.persistence.EntityManager
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Lazy

@AutoConfiguration
class ProcessDocumentsAutoConfiguration {

    @ProcessBean
    @Bean
    @ConditionalOnMissingBean(DocumentDelegate::class)
    fun documentDelegate(
        processDocumentService: ProcessDocumentService,
        userManagementService: UserManagementService,
        documentService: DocumentService
    ): DocumentDelegate {
        return DocumentDelegate(
            processDocumentService,
            userManagementService,
            documentService
        )
    }

    @ProcessBean
    @Bean
    @ConditionalOnMissingBean
    fun valueResolverDelegateService(
        valueResolverService: ValueResolverService
    ): ValueResolverDelegateService {
        return ValueResolverDelegateService(
            valueResolverService,
        )
    }

    @ProcessBean
    @Bean
    @ConditionalOnMissingBean(DocumentDelegateService::class)
    fun documentDelegateService(
        processDocumentService: ProcessDocumentService,
        documentService: DocumentService,
        jsonSchemaDocumentService: JsonSchemaDocumentService,
        userManagementService: UserManagementService,
        objectMapper: ObjectMapper,
    ): DocumentDelegateService {
        return DocumentDelegateService(
            processDocumentService,
            documentService,
            jsonSchemaDocumentService,
            userManagementService,
            objectMapper,
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
        @Lazy processDocumentService: CamundaProcessJsonSchemaDocumentService,
        queryDialectHelper: QueryDialectHelper
    ): CamundaTaskDocumentMapper {
        return CamundaTaskDocumentMapper(processDocumentService, queryDialectHelper)
    }

    @Bean
    @ConditionalOnMissingBean(ProcessDocumentLinkExporter::class)
    fun processDocumentLinkExporter(
        objectMapper: ObjectMapper,
        camundaRepositoryService: CamundaRepositoryService,
        processDocumentAssociationService: ProcessDocumentAssociationService
    ): ProcessDocumentLinkExporter {
        return ProcessDocumentLinkExporter(
            objectMapper,
            camundaRepositoryService,
            processDocumentAssociationService
        )
    }

    @Bean
    @ConditionalOnMissingBean(ProcessDocumentLinkImporter::class)
    fun processDocumentLinkImporter(
        processDocumentDeploymentService: ProcessDocumentDeploymentService
    ): ProcessDocumentLinkImporter {
        return ProcessDocumentLinkImporter(
            processDocumentDeploymentService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(CaseTaskListSearchService::class)
    fun caseTaskListSearchService(
        entityManager: EntityManager,
        valueResolverService: ValueResolverService,
        taskListColumnRepository: TaskListColumnRepository,
        userManagementService: UserManagementService,
        authorizationService: AuthorizationService,
        searchFieldV2Service: SearchFieldV2Service,
        queryDialectHelper: QueryDialectHelper
    ): CaseTaskListSearchService {
        return CaseTaskListSearchService(
            entityManager,
            valueResolverService,
            taskListColumnRepository,
            userManagementService,
            authorizationService,
            searchFieldV2Service,
            queryDialectHelper
        )
    }

    @Bean
    @ConditionalOnMissingBean(TaskListResource::class)
    fun processDocumentTaskListResource(
        caseTaskListSearchService: CaseTaskListSearchService,
        camundaTaskService: CamundaTaskService
    ): TaskListResource {
        return TaskListResource(
            caseTaskListSearchService,
            camundaTaskService
        )
    }

    @Bean
    @ConditionalOnMissingBean(TaskListSearchFieldV2Mapper::class)
    fun taskListSearchFieldV2Mapper(
        objectMapper: ObjectMapper
    ): TaskListSearchFieldV2Mapper {
        return TaskListSearchFieldV2Mapper(objectMapper)
    }
}
