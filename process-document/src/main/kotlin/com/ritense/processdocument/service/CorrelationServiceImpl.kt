package com.ritense.processdocument.service

import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.exception.DocumentNotFoundException
import com.ritense.document.service.DocumentService
import com.ritense.valtimo.service.CamundaProcessService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.runtime.MessageCorrelationResult
import org.camunda.bpm.engine.runtime.ProcessInstance
import java.util.*

class CorrelationServiceImpl(
    val runtimeService: RuntimeService,
    val documentService: DocumentService,
    val camundaProcessService: CamundaProcessService,
    val repositoryService: RepositoryService,
    val associationService: ProcessDocumentAssociationService
) : CorrelationService{

    override fun sendStartMessage(message: String) {
        return sendStartMessage(message,null,null)
    }

    override fun sendStartMessage(message: String,businessKey: String?) {
        return sendStartMessage(message,businessKey,null)
    }

    override fun sendStartMessage(message: String, businessKey: String?, variables: Map<String, Any>?){
        val result = correlate(message, businessKey,variables)
        val correlationResultProcess = result.processInstance
        val processName =
            camundaProcessService.findProcessDefinitionById(correlationResultProcess.processDefinitionId).name
        associateDocumentToProcess(correlationResultProcess.id, processName, businessKey)
    }

    override fun sendStartMessage(
        message: String,
        businessKey: String,
        variables: Map<String, Any>?,
        targetProcessDefinitionKey: String
    ){
        val processDefinitionId = getLatestProcessDefinitionIdByKey(targetProcessDefinitionKey)
        val correlationResultProcess = correlateWithProcessDefinitionId(message,businessKey, processDefinitionId.id,variables)

        val processName =
            camundaProcessService.findProcessDefinitionById(correlationResultProcess.processDefinitionId).name
        associateDocumentToProcess(correlationResultProcess.processInstanceId, processName, businessKey)
    }

    override fun sendMessageToAll(message: String): List<MessageCorrelationResult> {
        return sendMessageToAll(message,null,null)
    }

    override fun sendMessageToAll(message: String, businessKey: String?): List<MessageCorrelationResult> {
        return sendMessageToAll(message,businessKey,null)
    }

    override fun sendMessageToAll(message: String, businessKey: String?, variables: Map<String,Any>?): List<MessageCorrelationResult> {
        val correlationResultProcessList = correlateAll(message,businessKey,variables)
        correlationResultProcessList.forEach { correlationResultProcess ->
            val runningProcessInstance = camundaProcessService.findProcessInstanceById(correlationResultProcess.execution.processInstanceId)
            val processName =
                camundaProcessService.findProcessDefinitionById(runningProcessInstance.get().processDefinitionId).name
            associateDocumentToProcess(correlationResultProcess.execution.processInstanceId, processName,runningProcessInstance.get().businessKey)
        }

        return correlationResultProcessList
    }

    internal fun getLatestProcessDefinitionIdByKey(processDefinitionKey: String): ProcessDefinition {
        return camundaProcessService.getProcessDefinition(processDefinitionKey)?: throw RuntimeException()
    }

    internal fun associateDocumentToProcess(
        processInstanceId: String?,
        processName: String?,
        businessKey: String?
    ) {
        documentService.findBy(JsonSchemaDocumentId.existingId(UUID.fromString(businessKey)))
            .ifPresentOrElse({ document: Document ->
                associationService.createProcessDocumentInstance(
                    processInstanceId,
                    UUID.fromString(document.id().toString()),
                    processName
                )
            }) { throw DocumentNotFoundException("Document not found!") }
    }

    internal fun correlate(
        message: String,businessKey: String?,variables: Map<String, Any>?):MessageCorrelationResult{
        val builder = runtimeService.createMessageCorrelation(message)
        businessKey?.run {
            builder.processInstanceBusinessKey(businessKey)
        }
        variables?.run {
            builder.processInstanceVariablesEqual(variables)
        }
        return builder.correlateWithResult()

    }
    internal fun correlateWithProcessDefinitionId(
        message: String,
        businessKey: String,
        processDefinitionId: String,
        variables: Map<String, Any>?,
    ): ProcessInstance {
        return runtimeService
            .createMessageCorrelation(message)
            .processDefinitionId(processDefinitionId)
            .processInstanceBusinessKey(businessKey)
            .setVariables(variables?: emptyMap())
            .correlateStartMessage()
    }

    internal fun correlateAll(message: String, businessKey: String?, variables: Map<String, Any>?): List<MessageCorrelationResult> {
        val builder = runtimeService.createMessageCorrelation(message)
        businessKey?.run {
            builder.processInstanceBusinessKey(businessKey)
        }
        variables?.run {
            builder.processInstanceVariablesEqual(variables)
        }
        return builder.correlateAllWithResult()
    }
}