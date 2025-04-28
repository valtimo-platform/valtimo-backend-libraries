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

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.exception.DocumentNotFoundException
import com.ritense.document.service.DocumentService
import com.ritense.logging.withLoggingContext
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentInstance
import com.ritense.valtimo.camunda.service.CamundaRuntimeService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.service.CamundaProcessService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@SkipComponentScan
class ProcessDocumentsService(
    private val documentService: DocumentService,
    private val camundaProcessService: CamundaProcessService,
    private val associationService: ProcessDocumentAssociationService,
    private val processDocumentService: ProcessDocumentService,
    private val repositoryService: RepositoryService,
    private val camundaRuntimeService: CamundaRuntimeService,
) {

    fun deleteAllProcessInstancesForThisDocument(execution: DelegateExecution, reason: String) {
        val thisProcessInstanceId = CamundaProcessInstanceId(execution.processInstanceId)
        val documentId = processDocumentService.getDocumentId(thisProcessInstanceId, execution)
        requireNotNull(documentId) {
            "Failed to delete processes for document. Reason: current process has no association with a document."
        }
        withLoggingContext(JsonSchemaDocument::class, documentId.toString()) {
            val processInstanceIds = associationService.findProcessDocumentInstances(documentId)
                .map { it.processDocumentInstanceId().processInstanceId().toString() }
            camundaProcessService.findProcessInstancesByIds(processInstanceIds.toSet())
                .filter { it.rootProcessInstanceId == null || it.rootProcessInstanceId == it.processInstanceId }
                .mapNotNull { processInstance ->
                    try {
                        camundaProcessService.deleteProcessInstanceById(processInstance.id, reason)
                        null
                    } catch (exception: Exception) {
                        exception
                    }
                }
                .toList()
                .forEach { throw it }
        }
    }

    //TODO: Determine what to with this
    fun startProcessByProcessDefinitionKey(processDefinitionKey: String, businessKey: String) {
        startProcessByProcessDefinitionKey(processDefinitionKey, businessKey, null)
    }

    //TODO: Determine what to with this
    fun startProcessByProcessDefinitionKey(
        processDefinitionKey: String,
        businessKey: String,
        variables: Map<String, Any>?
    ) {
        val processInstance = runWithoutAuthorization {
            camundaProcessService.startProcess(processDefinitionKey, businessKey, variables)
        }
        require(processInstance.processDefinition.name != null) {
            "Process definition with id '${processInstance.processDefinition.id}' doesn't have a name"
        }
        associateDocumentToProcess(
            processInstance.processInstanceDto.id,
            processInstance.processDefinition.name!!,
            businessKey
        )
    }

    fun getActiveProcessInstanceIds(execution: DelegateExecution): List<String> {
        val processInstanceId = CamundaProcessInstanceId(execution.processInstanceId)
        val documentId = processDocumentService.getDocumentId(processInstanceId, execution)
        requireNotNull(documentId) {
            "No associated document found for process instance ID: ${execution.processInstanceId}"
        }

        return associationService.findProcessDocumentInstances(documentId)
            .filterIsInstance<CamundaProcessJsonSchemaDocumentInstance>()
            .filter { it.isActive() }
            .map {
                it.processDocumentInstanceId()
                    .processInstanceId()
                    .toString()
            }
    }

    fun getProcessDefinitionKeysFromActiveProcessInstances(execution: DelegateExecution): List<String> {
        var activeProcessInstances = getActiveProcessInstanceIds(execution)

        return activeProcessInstances.mapNotNull {
            val processInstance = camundaRuntimeService.findProcessInstanceById(it)!!
            repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionId(processInstance.processDefinitionId)
                .singleResult()
                .key
        }.distinct()
    }

    private fun associateDocumentToProcess(
        processInstanceId: String?,
        processName: String,
        businessKey: String
    ) {
        runWithoutAuthorization {
            documentService.findBy(JsonSchemaDocumentId.existingId(UUID.fromString(businessKey)))
                .ifPresentOrElse({ document: Document ->
                    associationService.createProcessDocumentInstance(
                        processInstanceId,
                        UUID.fromString(document.id().toString()),
                        processName
                    )
                }) { throw DocumentNotFoundException("No Document found with id $businessKey") }
        }
    }
}
