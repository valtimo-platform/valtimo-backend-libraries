/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.smartdocuments.service

import com.ritense.document.domain.Document
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.http.MediaType

class CamundaSmartDocumentGenerator(
    private val smartDocumentPdfGenerator: SmartDocumentPdfGenerator,
    private val processDocumentAssociationService: ProcessDocumentAssociationService,
    private val documentService: DocumentService,
) {

    fun generate(execution: DelegateExecution, mediaType: String, templateIdentifier: String) {
        if (mediaType != MediaType.APPLICATION_PDF_VALUE) {
            TODO("Not yet implemented")
        }

        smartDocumentPdfGenerator.generateAndStoreDocument(templateIdentifier, getDocument(execution), emptyList())
    }

    private fun getDocument(delegateExecution: DelegateExecution): Document {
        val processInstanceId = CamundaProcessInstanceId(delegateExecution.processInstanceId)
        val processDocumentInstance = processDocumentAssociationService.findProcessDocumentInstance(processInstanceId)
        return if (processDocumentInstance.isPresent) {
            val jsonSchemaDocumentId = processDocumentInstance.get().processDocumentInstanceId().documentId()
            documentService.findBy(jsonSchemaDocumentId).orElseThrow()
        } else {
            // In case a process has no token wait state ProcessDocumentInstance is not yet created,
            // therefore out business-key is our last chance which is populated with the documentId also.
            documentService.get(delegateExecution.businessKey)
        }
    }

}