package com.ritense.objectsapi.taak

import com.ritense.document.domain.Document
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.domain.ProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentAssociationService

class ProcessDocumentService(
    val processDocumentAssociationService:ProcessDocumentAssociationService,
    val documentService: DocumentService
) {
    fun getDocument(processInstanceId: ProcessInstanceId, fallbackDocumentId: String? = null): Document {
        val processDocumentInstance = processDocumentAssociationService.findProcessDocumentInstance(processInstanceId)
        return if (processDocumentInstance.isPresent) {
            val jsonSchemaDocumentId = processDocumentInstance.get().processDocumentInstanceId().documentId()
            documentService.findBy(jsonSchemaDocumentId).orElseThrow()
        } else {
            // In case a process has no token wait state ProcessDocumentInstance is not yet created,
            // therefore out business-key is our last chance which is populated with the documentId also.
            documentService.get(fallbackDocumentId)
        }
    }
}