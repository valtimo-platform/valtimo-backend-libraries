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
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byKey
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byLatestVersion
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.camunda.service.CamundaRuntimeService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.runtime.MessageCorrelationResult
import org.camunda.bpm.engine.runtime.ProcessInstance

class CorrelationServiceImpl(
    val runtimeService: RuntimeService,
    val camundaRuntimeService: CamundaRuntimeService,
    val documentService: DocumentService,
    val camundaRepositoryService: CamundaRepositoryService,
    val repositoryService: RepositoryService,
    val associationService: ProcessDocumentAssociationService
) : CorrelationService {

    override fun sendStartMessage(message: String, businessKey: String): MessageCorrelationResult {
        return sendStartMessage(message, businessKey, null)
    }

    override fun sendStartMessage(
        message: String,
        businessKey: String,
        vararg variables: Any?
    ): MessageCorrelationResult {
        return sendStartMessage(message, businessKey, toVariableMap(*variables))
    }

    override fun sendStartMessage(
        message: String,
        businessKey: String,
        variables: Map<String, Any?>?
    ): MessageCorrelationResult {
        val result = correlate(message, businessKey, variables)
        val processName = getProcessDefinitionName(result.processInstance.processDefinitionId)
        associateDocumentToProcess(result.processInstance.id, processName, businessKey)
        return result
    }

    override fun sendStartMessageWithProcessDefinitionKey(
        message: String,
        targetProcessDefinitionKey: String,
        businessKey: String
    ) {
        sendStartMessageWithProcessDefinitionKey(message, targetProcessDefinitionKey, businessKey, null)
    }

    override fun sendStartMessageWithProcessDefinitionKey(
        message: String,
        targetProcessDefinitionKey: String,
        businessKey: String,
        variables: Map<String, Any?>?
    ) {
        val processDefinitionId = getLatestProcessDefinitionIdByKey(targetProcessDefinitionKey)
        val result = correlateWithProcessDefinitionId(message, businessKey, processDefinitionId.id, variables)
        val processName = getProcessDefinitionName(result.processDefinitionId)
        associateDocumentToProcess(result.processInstanceId, processName, businessKey)
    }

    override fun sendStartMessageWithProcessDefinitionKey(
        message: String,
        targetProcessDefinitionKey: String,
        businessKey: String,
        vararg variables: Any?
    ) {
        sendStartMessageWithProcessDefinitionKey(
            message,
            targetProcessDefinitionKey,
            businessKey,
            toVariableMap(*variables)
        )
    }

    override fun sendCatchEventMessage(message: String, businessKey: String): MessageCorrelationResult {
        return sendCatchEventMessage(message, businessKey, null)
    }

    override fun sendCatchEventMessage(
        message: String,
        businessKey: String,
        variables: Map<String, Any?>?
    ): MessageCorrelationResult {
        val result = correlate(message, businessKey, variables)
        val processInstanceId = result.execution.processInstanceId
        val processName = getProcessDefinitionNameByProcessInstanceId(processInstanceId)
        associateDocumentToProcess(processInstanceId, processName, businessKey)
        return result
    }

    override fun sendCatchEventMessage(
        message: String,
        businessKey: String,
        vararg variables: Any?
    ): MessageCorrelationResult {
        return sendCatchEventMessage(message, businessKey, toVariableMap(*variables))
    }

    override fun sendCatchEventMessageToAll(message: String, businessKey: String): List<MessageCorrelationResult> {
        return sendCatchEventMessageToAll(message, businessKey, null)
    }

    override fun sendCatchEventMessageToAll(
        message: String,
        businessKey: String,
        variables: Map<String, Any?>?
    ): List<MessageCorrelationResult> {
        val correlationResultProcessList = correlateAll(message, businessKey, variables)
        correlationResultProcessList.forEach { correlationResultProcess ->
            val processInstanceId = correlationResultProcess.execution.processInstanceId
            val processName = getProcessDefinitionNameByProcessInstanceId(processInstanceId)
            associateDocumentToProcess(processInstanceId, processName, businessKey)
        }

        return correlationResultProcessList
    }

    override fun sendCatchEventMessageToAll(
        message: String,
        businessKey: String,
        vararg variables: Any?
    ): List<MessageCorrelationResult> {
        return sendCatchEventMessageToAll(message, businessKey, toVariableMap(*variables))
    }

    override fun sendMessage(message: String, execution: DelegateExecution): MessageCorrelationResult {
        val result = correlate(message, execution.businessKey, execution.variables)
        associateDocumentToProcess(result, execution.businessKey)
        return result
    }

    override fun sendMessageToAll(message: String, execution: DelegateExecution): List<MessageCorrelationResult> {
        val results = correlateAll(message, execution.businessKey, execution.variables)
        results.forEach { associateDocumentToProcess(it, execution.businessKey) }
        return results
    }

    private fun getLatestProcessDefinitionIdByKey(processDefinitionKey: String): CamundaProcessDefinition {
        return runWithoutAuthorization {
            camundaRepositoryService.findProcessDefinition(byKey(processDefinitionKey).and(byLatestVersion()))
                ?: throw RuntimeException("Failed to get process definition with key $processDefinitionKey")
        }
    }

    private fun associationExists(processInstanceId: String): Boolean {
        return runWithoutAuthorization {
            associationService.findProcessDocumentInstance(CamundaProcessInstanceId(processInstanceId)).isPresent
        }
    }

    private fun associateDocumentToProcess(result: MessageCorrelationResult, businessKey: String) {
        if (result.processInstance?.processDefinitionId != null) {
            val processName = getProcessDefinitionName(result.processInstance.processDefinitionId)
            associateDocumentToProcess(result.processInstance.id, processName, businessKey)
        } else {
            val processInstanceId = result.execution.processInstanceId
            val processName = getProcessDefinitionNameByProcessInstanceId(processInstanceId)
            associateDocumentToProcess(processInstanceId, processName, businessKey)
        }
    }

    private fun associateDocumentToProcess(
        processInstanceId: String,
        processName: String,
        businessKey: String
    ) {
        runWithoutAuthorization {
            if (!associationExists(processInstanceId)) {
                val document = documentService[businessKey]
                associationService.createProcessDocumentInstance(
                    processInstanceId,
                    document.id().id,
                    processName
                )
            }
        }
    }

    private fun correlate(
        message: String,
        businessKey: String,
        variables: Map<String, Any?>?
    ): MessageCorrelationResult {
        val builder = runtimeService.createMessageCorrelation(message)
        builder.processInstanceBusinessKey(businessKey)
        variables?.run { builder.setVariables(variables) }
        return builder.correlateWithResult()
    }

    private fun correlateWithProcessDefinitionId(
        message: String,
        businessKey: String,
        processDefinitionId: String,
        variables: Map<String, Any?>?,
    ): ProcessInstance {
        val builder = runtimeService.createMessageCorrelation(message)
        builder.processDefinitionId(processDefinitionId)
        builder.processInstanceBusinessKey(businessKey)
        variables?.run { builder.setVariables(variables) }
        return builder.correlateStartMessage()
    }

    private fun correlateAll(
        message: String,
        businessKey: String,
        variables: Map<String, Any?>?
    ): List<MessageCorrelationResult> {
        val builder = runtimeService.createMessageCorrelation(message)
        builder.processInstanceBusinessKey(businessKey)
        variables?.run { builder.setVariables(variables) }
        return builder.correlateAllWithResult()
    }

    private fun getProcessDefinitionName(processDefinitionId: String): String {
        val process = runWithoutAuthorization {
            camundaRepositoryService.findProcessDefinitionById(processDefinitionId)
                ?: throw IllegalStateException("No process definition exists with id '$processDefinitionId'")
        }

        return process.name
            ?: throw IllegalStateException("Process definition with id '$processDefinitionId' doesn't have a name")
    }

    private fun getProcessDefinitionNameByProcessInstanceId(processInstanceId: String): String {
        return runWithoutAuthorization {
            val processInstance = camundaRuntimeService.findProcessInstanceById(processInstanceId)!!
            getProcessDefinitionName(processInstance.processDefinitionId)
        }
    }

    private fun toVariableMap(vararg variables: Any?): Map<String, Any?>? {
        return if (variables.isNotEmpty()) {
            (0 until variables.size / 2).associate { i -> variables[i * 2] as String to variables[i * 2 + 1] }
        } else {
            null
        }
    }
}