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

package com.ritense.processdocument.service

import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.exception.DocumentNotFoundException
import com.ritense.document.service.DocumentService
import com.ritense.valtimo.service.CamundaProcessService
import java.util.*

class ProcessDocumentKotlinService(
    private val documentService: DocumentService,
    private val camundaProcessService: CamundaProcessService,
    private val associationService: ProcessDocumentAssociationService
) {

    fun startProcessByProcessDefinitionKey(processDefinitionKey: String,businessKey: String){
        startProcessByProcessDefinitionKey(processDefinitionKey,businessKey,null)
    }

    fun startProcessByProcessDefinitionKey(processDefinitionKey: String, businessKey: String, variables: Map<String,Any>?) {
        val processInstance = camundaProcessService.startProcess(processDefinitionKey,businessKey,variables)
        associateDocumentToProcess(processInstance.processInstanceDto.id,processInstance.processDefinition.name,businessKey)
    }

    private fun associateDocumentToProcess(
        processInstanceId: String?,
        processName: String?,
        businessKey: String
    ) {
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