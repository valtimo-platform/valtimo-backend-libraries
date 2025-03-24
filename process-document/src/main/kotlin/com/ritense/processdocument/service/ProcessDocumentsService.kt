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

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.exception.DocumentNotFoundException
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.dto.ProcessInstanceSimpleDto
import com.ritense.processdocument.service.impl.CamundaProcessJsonSchemaDocumentAssociationService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.service.CamundaProcessService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@SkipComponentScan
class ProcessDocumentsService(
    private val documentService: DocumentService,
    private val camundaProcessService: CamundaProcessService,
    private val associationService: ProcessDocumentAssociationService,
    private val documentAssociationService: CamundaProcessJsonSchemaDocumentAssociationService,
    private val processDocumentService: ProcessDocumentService,
) {
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

    fun getActiveProcessInstances(execution: DelegateExecution): List<ProcessInstanceSimpleDto> {
        val processInstanceId = CamundaProcessInstanceId(execution.processInstanceId)
        val documentId = processDocumentService.getDocumentId(processInstanceId, execution)
        requireNotNull(documentId) {
            "No associated document found for process instance ID: ${execution.processInstanceId}"
        }
        return documentAssociationService.findProcessDocumentInstances(documentId)
            .asSequence()
            .filter { it.isActive() }
            .mapNotNull { ProcessInstanceSimpleDto(
                it.processName(),
                it.processDocumentInstanceId()
                    .processInstanceId()
                    .toString())
            }
            .toList()
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