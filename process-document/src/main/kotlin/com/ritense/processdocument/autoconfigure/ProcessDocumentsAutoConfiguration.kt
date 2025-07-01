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
import com.ritense.document.repository.impl.JsonSchemaDocumentRepository
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.document.service.DocumentService
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.processdocument.domain.impl.delegate.DocumentDelegate
import com.ritense.processdocument.exporter.ProcessDocumentLinkExporter
import com.ritense.processdocument.importer.ProcessDocumentLinkImporter
import com.ritense.processdocument.listener.CaseAssigneeListener
import com.ritense.processdocument.listener.CaseAssigneeTaskCreatedListener
import com.ritense.processdocument.listener.DecisionCaseEventListener
import com.ritense.processdocument.listener.ProcessDefinitionCaseEventListener
import com.ritense.processdocument.listener.ProcessDocumentLinkEventListener
import com.ritense.processdocument.operaton.authorization.OperatonTaskDocumentMapper
import com.ritense.processdocument.repository.ProcessDefinitionCaseDefinitionRepository
import com.ritense.processdocument.repository.ProcessDocumentInstanceRepository
import com.ritense.processdocument.service.CaseDefinitionProcessLinkService
import com.ritense.processdocument.service.CaseTaskListSearchService
import com.ritense.processdocument.service.CorrelationService
import com.ritense.processdocument.service.CorrelationServiceImpl
import com.ritense.processdocument.service.DefaultProcessDefinitionCaseDefinitionLinker
import com.ritense.processdocument.service.DocumentDelegateService
import com.ritense.processdocument.service.ProcessDefinitionCaseDefinitionService
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentDeletedEventListener
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processdocument.service.ProcessDocumentsService
import com.ritense.processdocument.service.ValueResolverDelegateService
import com.ritense.processdocument.tasksearch.TaskListSearchFieldV2Mapper
import com.ritense.processdocument.tasksearch.TaskSearchFieldExporter
import com.ritense.processdocument.tasksearch.TaskSearchFieldImporter
import com.ritense.processdocument.web.CaseDefinitionProcessManagementResource
import com.ritense.processdocument.web.ProcessCaseManagementResource
import com.ritense.processdocument.web.TaskListResource
import com.ritense.search.repository.SearchFieldV2Repository
import com.ritense.search.service.SearchFieldV2Service
import com.ritense.valtimo.contract.annotation.ProcessBean
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.case_.CaseDefinitionChecker
import com.ritense.valtimo.contract.database.QueryDialectHelper
import com.ritense.valtimo.decision.OperatonDecisionService
import com.ritense.valtimo.operaton.service.OperatonRepositoryService
import com.ritense.valtimo.operaton.service.OperatonRuntimeService
import com.ritense.valtimo.service.OperatonProcessService
import com.ritense.valtimo.service.OperatonTaskService
import com.ritense.valtimo.service.ProcessDefinitionCaseDefinitionLinker
import com.ritense.valueresolver.ValueResolverService
import jakarta.persistence.EntityManager
import org.operaton.bpm.engine.RepositoryService
import org.operaton.bpm.engine.RuntimeService
import org.operaton.bpm.engine.TaskService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order

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
        operatonRuntimeService: OperatonRuntimeService,
        documentService: DocumentService,
        processDocumentAssociationService: ProcessDocumentAssociationService,
        operatonProcessService: OperatonProcessService,
        repositoryService: RepositoryService,
        operatonRepositoryService: OperatonRepositoryService,
    ): CorrelationService {
        return CorrelationServiceImpl(
            runtimeService = runtimeService,
            operatonRuntimeService = operatonRuntimeService,
            documentService = documentService,
            operatonRepositoryService = operatonRepositoryService,
            repositoryService = repositoryService,
            associationService = processDocumentAssociationService
        )
    }

    @ProcessBean
    @Bean("processService")
    @ConditionalOnMissingBean(ProcessDocumentsService::class)
    fun processDocumentsService(
        documentService: DocumentService,
        operatonProcessService: OperatonProcessService,
        associationService: ProcessDocumentAssociationService,
        processDocumentService: ProcessDocumentService,
        repositoryService: RepositoryService,
        operatonRuntimeService: OperatonRuntimeService
    ): ProcessDocumentsService {
        return ProcessDocumentsService(
            documentService,
            operatonProcessService,
            associationService,
            processDocumentService,
            repositoryService,
            operatonRuntimeService
        )
    }

    @Bean
    fun caseAssigneeOperatonTaskListener(
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
        operatonTaskService: OperatonTaskService,
        documentService: DocumentService,
        caseDefinitionService: CaseDefinitionService,
        userManagementService: UserManagementService
    ): CaseAssigneeListener {
        return CaseAssigneeListener(
            operatonTaskService, documentService, caseDefinitionService, userManagementService
        )
    }

    @Bean
    @ConditionalOnMissingBean(OperatonTaskDocumentMapper::class)
    fun operatonTaskDocumentMapper(
        processDocumentInstanceRepository: ProcessDocumentInstanceRepository,
        documentRepository: JsonSchemaDocumentRepository,
        queryDialectHelper: QueryDialectHelper
    ): OperatonTaskDocumentMapper {
        return OperatonTaskDocumentMapper(processDocumentInstanceRepository, documentRepository, queryDialectHelper)
    }

    @Bean
    @ConditionalOnMissingBean(ProcessDocumentLinkExporter::class)
    fun processDocumentLinkExporter(
        objectMapper: ObjectMapper,
        operatonRepositoryService: OperatonRepositoryService,
        processDefinitionCaseDefinitionService: ProcessDefinitionCaseDefinitionService
    ): ProcessDocumentLinkExporter {
        return ProcessDocumentLinkExporter(
            objectMapper,
            operatonRepositoryService,
            processDefinitionCaseDefinitionService
        )
    }

    @Bean
    @ConditionalOnMissingBean(ProcessDocumentLinkImporter::class)
    fun processDocumentLinkImporter(
        processDefinitionCaseDefinitionService: ProcessDefinitionCaseDefinitionService,
        documentDefinitionService: DocumentDefinitionService,
        objectMapper: ObjectMapper,
        processService: OperatonProcessService
    ): ProcessDocumentLinkImporter {
        return ProcessDocumentLinkImporter(
            processDefinitionCaseDefinitionService,
            documentDefinitionService,
            objectMapper,
            processService
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
        operatonTaskService: OperatonTaskService
    ): TaskListResource {
        return TaskListResource(
            caseTaskListSearchService,
            operatonTaskService
        )
    }

    @Bean
    @ConditionalOnMissingBean(TaskListSearchFieldV2Mapper::class)
    fun taskListSearchFieldV2Mapper(
        objectMapper: ObjectMapper
    ): TaskListSearchFieldV2Mapper {
        return TaskListSearchFieldV2Mapper(objectMapper)
    }

    @Bean
    @ConditionalOnMissingBean(TaskSearchFieldExporter::class)
    fun taskSearchFieldExporter(
        objectMapper: ObjectMapper,
        searchFieldService: SearchFieldV2Service,
    ): TaskSearchFieldExporter {
        return TaskSearchFieldExporter(
            objectMapper,
            searchFieldService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(TaskSearchFieldImporter::class)
    fun taskSearchFieldImporter(
        objectMapper: ObjectMapper,
        repository: SearchFieldV2Repository,
        searchFieldService: SearchFieldV2Service,
    ): TaskSearchFieldImporter {
        return TaskSearchFieldImporter(
            objectMapper, repository, searchFieldService
        )
    }

    @Bean
    @ConditionalOnMissingBean(ProcessDefinitionCaseDefinitionService::class)
    fun processDefinitionCaseDefinitionService(
        authorizationService: AuthorizationService,
        processDefinitionCaseDefinitionRepository: ProcessDefinitionCaseDefinitionRepository,
        documentService: JsonSchemaDocumentService,
        runtimeService: RuntimeService,
        repositoryService: OperatonRepositoryService,
        caseDefinitionChecker: CaseDefinitionChecker,
    ): ProcessDefinitionCaseDefinitionService {
        return ProcessDefinitionCaseDefinitionService(
            authorizationService,
            processDefinitionCaseDefinitionRepository,
            documentService,
            runtimeService,
            repositoryService,
            caseDefinitionChecker,
        )
    }

    @Bean
    @ConditionalOnMissingBean(ProcessDocumentDeletedEventListener::class)
    @Order(100)
    fun processDocumentDeletedEventListener(
        runtimeService: RuntimeService,
        processDocumentAssociationService: ProcessDocumentAssociationService
    ): ProcessDocumentDeletedEventListener {
        return ProcessDocumentDeletedEventListener(
            runtimeService,
            processDocumentAssociationService
        )
    }

    @Bean
    @ConditionalOnMissingBean(ProcessDefinitionCaseDefinitionLinker::class)
    fun defaultProcessDefinitionCaseDefinitionLinker(
        processDefinitionCaseDefinitionService: ProcessDefinitionCaseDefinitionService
    ): ProcessDefinitionCaseDefinitionLinker {
        return DefaultProcessDefinitionCaseDefinitionLinker(
            processDefinitionCaseDefinitionService
        )
    }

    @Bean
    @ConditionalOnMissingBean(CaseDefinitionProcessManagementResource::class)
    fun caseDefinitionProcessManagementResource(
        caseDefinitionProcessLinkService: CaseDefinitionProcessLinkService
    ): CaseDefinitionProcessManagementResource {
        return CaseDefinitionProcessManagementResource(caseDefinitionProcessLinkService)
    }

    @Bean
    @ConditionalOnMissingBean(ProcessCaseManagementResource::class)
    fun processCaseManagementResource(
        processDefinitionCaseDefinitionService: ProcessDefinitionCaseDefinitionService
    ): ProcessCaseManagementResource {
        return ProcessCaseManagementResource(processDefinitionCaseDefinitionService)
    }

    @Bean
    @ConditionalOnMissingBean(ProcessDefinitionCaseEventListener::class)
    fun processDefinitionCaseEventListener(
        processService: OperatonProcessService,
        associationService: ProcessDefinitionCaseDefinitionService,
    ): ProcessDefinitionCaseEventListener {
        return ProcessDefinitionCaseEventListener(processService, associationService)
    }

    @Bean
    @ConditionalOnMissingBean(DecisionCaseEventListener::class)
    fun decisionCaseEventListener(
        decisionService: OperatonDecisionService,
        processService: OperatonProcessService,
    ): DecisionCaseEventListener {
        return DecisionCaseEventListener(decisionService, processService)
    }

    @Bean
    @ConditionalOnMissingBean
    fun processDocumentLinkEventListener(
        caseDefinitionProcessLinkService: CaseDefinitionProcessLinkService
    ): ProcessDocumentLinkEventListener = ProcessDocumentLinkEventListener(caseDefinitionProcessLinkService)
}