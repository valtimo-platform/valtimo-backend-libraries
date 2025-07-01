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
package com.ritense.processdocument.service

import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.AuthorizationResourceContext
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.authorization.request.RelatedEntityAuthorizationRequest
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.processdocument.domain.ProcessDefinitionCaseDefinition
import com.ritense.processdocument.domain.ProcessDefinitionCaseDefinitionId
import com.ritense.processdocument.domain.ProcessDefinitionId
import com.ritense.processdocument.domain.ProcessDocumentDefinitionRequest
import com.ritense.processdocument.domain.UpdateProcessDefinitionCaseDefinitionRequest
import com.ritense.processdocument.domain.impl.OperatonProcessInstanceId
import com.ritense.processdocument.repository.ProcessDefinitionCaseDefinitionRepository
import com.ritense.valtimo.contract.case_.CaseDefinitionChecker
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.operaton.authorization.OperatonExecutionActionProvider
import com.ritense.valtimo.operaton.domain.OperatonExecution
import com.ritense.valtimo.operaton.domain.OperatonProcessDefinition
import com.ritense.valtimo.operaton.service.OperatonRepositoryService
import org.operaton.bpm.engine.RuntimeService
import java.util.UUID

class ProcessDefinitionCaseDefinitionService(
    private val authorizationService: AuthorizationService,
    private val processDefinitionCaseDefinitionRepository: ProcessDefinitionCaseDefinitionRepository,
    private val documentService: JsonSchemaDocumentService,
    private val runtimeService: RuntimeService,
    private val repositoryService: OperatonRepositoryService,
    private val caseDefinitionChecker: CaseDefinitionChecker,
) {
    fun findById(id: ProcessDefinitionCaseDefinitionId): ProcessDefinitionCaseDefinition? {
        return processDefinitionCaseDefinitionRepository.findById(id).orElse(null)
    }

    fun findByProcessDefinitionId(processDefinitionId: ProcessDefinitionId): ProcessDefinitionCaseDefinition {
        return processDefinitionCaseDefinitionRepository.findByIdProcessDefinitionId(processDefinitionId)
    }

    fun findProcessDefinitionCaseDefinitions(caseDefinitionId: CaseDefinitionId): List<ProcessDefinitionCaseDefinition> {
        return processDefinitionCaseDefinitionRepository.findByIdCaseDefinitionId(caseDefinitionId)
    }

    fun findProcessDefinitionCaseDefinition(operatonProcessInstanceId: OperatonProcessInstanceId): ProcessDefinitionCaseDefinition {
        val processInstance = (runtimeService.createProcessInstanceQuery()
            .processInstanceId(operatonProcessInstanceId.toString())
            .singleResult()
            ?: throw IllegalArgumentException("Process instance not found"))

        try {
            return findByProcessDefinitionId(ProcessDefinitionId(processInstance.processDefinitionId))
        } catch (e: Exception) {
            val document = documentService.getDocumentBy(JsonSchemaDocumentId.existingId(processInstance.businessKey))
            val processDefinitionCaseDefinition = ProcessDefinitionCaseDefinition(
                id = ProcessDefinitionCaseDefinitionId(
                    ProcessDefinitionId(processInstance.processDefinitionId),
                    document.definitionId().caseDefinitionId()
                )
            )

            val processDefinition = repositoryService.findProcessDefinitionById(processInstance.processDefinitionId)!!
            processDefinitionCaseDefinition.processDefinitionName = processDefinition.name
            processDefinitionCaseDefinition.processDefinitionKey = processDefinition.key

            return processDefinitionCaseDefinition
        }
    }

    fun findProcessDefinitionCaseDefinitions(
        caseDefinitionId: CaseDefinitionId,
        startableByUser: Boolean?,
        canInitializeDocument: Boolean?
    ): List<ProcessDefinitionCaseDefinition> {
        val definitions =
            processDefinitionCaseDefinitionRepository.findAll(caseDefinitionId, startableByUser, canInitializeDocument)

        return definitions
            .filter {
                authorizationService.hasPermission(
                    RelatedEntityAuthorizationRequest<OperatonExecution>(
                        OperatonExecution::class.java,
                        OperatonExecutionActionProvider.CREATE,
                        OperatonProcessDefinition::class.java,
                        it.id.processDefinitionId.id
                    )
                )
            }
    }

    fun findProcessDefinitionCaseDefinitions(
        documentId: UUID,
        startableByUser: Boolean?,
        canInitializeDocument: Boolean?
    ): List<ProcessDefinitionCaseDefinition> {

        val document = documentService.get(documentId.toString())
        val definitions = processDefinitionCaseDefinitionRepository.findAll(
            document.definitionId().caseDefinitionId(),
            startableByUser,
            canInitializeDocument
        )

        return definitions
            .filter {
                authorizationService.hasPermission(
                    RelatedEntityAuthorizationRequest<OperatonExecution>(
                        OperatonExecution::class.java,
                        OperatonExecutionActionProvider.CREATE,
                        OperatonProcessDefinition::class.java,
                        it.id.processDefinitionId.id
                    ).withContext(
                        AuthorizationResourceContext(
                            JsonSchemaDocument::class.java,
                            document
                        )
                    )
                )
            }
    }

    fun deleteProcessDefinitionCaseDefinition(
        processDefinitionId: ProcessDefinitionId,
        caseDefinitionId: CaseDefinitionId
    ) {
        denyAuthorization()
        caseDefinitionChecker.assertCanUpdateCaseDefinition(caseDefinitionId)

        val id = ProcessDefinitionCaseDefinitionId(
            processDefinitionId,
            caseDefinitionId
        )

        processDefinitionCaseDefinitionRepository.deleteById(id)
    }

    fun createProcessDocumentDefinition(request: ProcessDocumentDefinitionRequest) {
        denyAuthorization()
        caseDefinitionChecker.assertCanUpdateCaseDefinition(request.caseDefinitionId)

        val processDocumentDefinition = ProcessDefinitionCaseDefinition(
            id = ProcessDefinitionCaseDefinitionId(
                request.processDefinitionId,
                request.caseDefinitionId
            ),
            canInitializeDocument = request.canInitializeDocument,
            startableByUser = request.startableByUser
        )

        processDefinitionCaseDefinitionRepository.save(processDocumentDefinition)
    }


    fun updateProcessDefinitionCaseDefinition(
        caseDefinitionKey: String,
        caseDefinitionVersionTag: String,
        processDefinitionId: String,
        updateRequest: UpdateProcessDefinitionCaseDefinitionRequest
    ) {
        val caseDefinitionId = CaseDefinitionId(caseDefinitionKey, caseDefinitionVersionTag)

        val processDefinitionCaseDefinition =
            processDefinitionCaseDefinitionRepository.findAllByIdCaseDefinitionIdAndIdProcessDefinitionIdId(
                caseDefinitionId = caseDefinitionId,
                processDefinitionId = processDefinitionId
            )

        val originalProcessDefinitionCaseDefinition = processDefinitionCaseDefinition.firstOrNull()
            ?: error("No ProcessDefinitionCaseDefinition found for case definition key '$caseDefinitionKey', case definition version tag '$caseDefinitionVersionTag', and process definition id '$processDefinitionId'.")

        val updated = ProcessDefinitionCaseDefinition(
            ProcessDefinitionCaseDefinitionId(
                ProcessDefinitionId(processDefinitionId),
                caseDefinitionId
            ),
            updateRequest.canInitializeDocument ?: originalProcessDefinitionCaseDefinition.canInitializeDocument,
            updateRequest.startableByUser ?: originalProcessDefinitionCaseDefinition.startableByUser
        )

        processDefinitionCaseDefinitionRepository.save(updated)
    }

    private fun denyAuthorization() {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                ProcessDefinitionCaseDefinition::class.java,
                Action.deny()
            )
        )
    }
}
