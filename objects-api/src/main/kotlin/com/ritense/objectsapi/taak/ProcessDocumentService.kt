package com.ritense.objectsapi.taak

import com.ritense.document.domain.Document
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.domain.ProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.valtimo.service.CamundaProcessService
import org.camunda.bpm.engine.delegate.BaseDelegateExecution
import org.camunda.bpm.engine.delegate.VariableScope

class ProcessDocumentService(
    private val processDocumentAssociationService: ProcessDocumentAssociationService,
    private val documentService: DocumentService,
    private val camundaProcessService: CamundaProcessService
) {

    fun getDocument(processInstanceId: ProcessInstanceId, variableScope: VariableScope): Document {
        val processDocumentInstance = processDocumentAssociationService.findProcessDocumentInstance(processInstanceId).orNull()
        return if (processDocumentInstance != null) {
            val jsonSchemaDocumentId = processDocumentInstance.processDocumentInstanceId().documentId()
            documentService.findBy(jsonSchemaDocumentId).orNull()
                ?: throw RuntimeException("Could not find document by documentInstance for process instance $processInstanceId!")
        } else {
            // In case a process has no token wait state ProcessDocumentInstance is not yet created,
            // therefore out business-key is our last chance which is populated with the documentId also.
            val businessKey = getBusinessKey(processInstanceId, variableScope)
            documentService.get(businessKey)
                ?: throw RuntimeException("Could not find document by businessKey ($businessKey) for process instance $processInstanceId!")
        }
    }

    private fun getBusinessKey(processInstanceId: ProcessInstanceId, variableScope: VariableScope): String {
        return if (variableScope is BaseDelegateExecution) {
            variableScope.businessKey
        } else {
            val processInstance = camundaProcessService.findProcessInstanceById(processInstanceId.toString()).orNull()
                ?: throw RuntimeException("Process instance not found by id $processInstanceId")
            processInstance.businessKey
        }
    }
}