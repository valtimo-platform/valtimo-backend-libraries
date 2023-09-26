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

package com.ritense.portaaltaak

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.notificatiesapi.event.NotificatiesApiNotificationReceivedEvent
import com.ritense.notificatiesapi.exception.NotificatiesNotificationEventException
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.service.ObjectManagementService
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.domain.ProcessInstanceId
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.service.CamundaProcessService
import com.ritense.valtimo.service.CamundaTaskService
import com.ritense.valueresolver.ValueResolverService
import java.net.MalformedURLException
import java.net.URI
import java.util.UUID
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.VariableScope
import org.springframework.context.event.EventListener

open class PortaalTaakEventListener(
    private val objectManagementService: ObjectManagementService,
    private val pluginService: PluginService,
    private val processDocumentService: ProcessDocumentService,
    private val processService: CamundaProcessService,
    private val taskService: CamundaTaskService,
    private val runtimeService: RuntimeService,
    private val valueResolverService: ValueResolverService,
    private val objectMapper: ObjectMapper
) {

    @RunWithoutAuthorization
    @EventListener(NotificatiesApiNotificationReceivedEvent::class)
    open fun processCompletePortaalTaakEvent(event: NotificatiesApiNotificationReceivedEvent) {
        val objectType = event.kenmerken["objectType"]

        if (!event.kanaal.equals("objecten", ignoreCase = true) ||
            !event.actie.equals("update", ignoreCase = true) ||
            objectType == null
        ) {
            return
        }

        val objectManagement =
            objectManagementService.findByObjectTypeId(objectType.substringAfterLast("/")) ?: return

        pluginService.findPluginConfiguration(PortaaltaakPlugin::class.java) { properties: JsonNode
            ->
            properties.get("objectManagementConfigurationId").textValue().equals(objectManagement.id.toString())
        }?.let {

            val taakObject: TaakObject =
                objectMapper.convertValue(getPortaalTaakObjectData(objectManagement, event))
            when (taakObject.status) {
                TaakStatus.INGEDIEND -> {
                    val task = taskService.findTaskById(taakObject.verwerkerTaakId) ?: return
                    val receiveData = getReceiveDataActionProperty(task, it.id.id) ?: return

                    val portaaltaakPlugin = pluginService.createInstance(it) as PortaaltaakPlugin
                    val processInstanceId = CamundaProcessInstanceId(task.getProcessInstanceId())
                    val documentId = runWithoutAuthorization {
                        processDocumentService.getDocumentId(processInstanceId, task)
                    }
                    saveDataInDocument(taakObject, task, receiveData)
                    startProcessToUploadDocuments(
                        taakObject,
                        portaaltaakPlugin.completeTaakProcess,
                        documentId.id.toString(),
                        objectManagement.objectenApiPluginConfigurationId.toString(),
                        event.resourceUrl
                    )
                }

                else -> null
            }
        }
    }

    private fun getReceiveDataActionProperty(task: CamundaTask, pluginConfigurationId: UUID): List<DataBindingConfig>? {
        val processLinks = pluginService.getProcessLinks(task.getProcessDefinitionId(), task.taskDefinitionKey!!)
        val processLink = processLinks.firstOrNull { processLink ->
            processLink.pluginConfigurationId == pluginConfigurationId
        }

        val receiveDataJsonNode = processLink?.actionProperties?.get("receiveData") ?: return null

        val typeRef = object : TypeReference<List<DataBindingConfig>>() {}
        return objectMapper.treeToValue(receiveDataJsonNode, objectMapper.constructType(typeRef))
    }

    internal fun saveDataInDocument(
        taakObject: TaakObject,
        task: CamundaTask,
        receiveData: List<DataBindingConfig>
    ) {
        if (taakObject.verzondenData.isNotEmpty()) {
            val processInstanceId = CamundaProcessInstanceId(task.getProcessInstanceId())
            val variableScope = getVariableScope(task)
            val taakObjectData = objectMapper.valueToTree<JsonNode>(taakObject.verzondenData)
            val resolvedValues = getResolvedValues(receiveData, taakObjectData)
            handleTaakObjectData(processInstanceId, variableScope, resolvedValues)
        }
    }

    /**
     * @param receiveData: [ doc:/streetName  to  "/persoonsData/adres/straatnaam" ]
     * @param data {"persoonsData":{"adres":{"straatnaam":"Funenpark"}}}
     * @return mapOf(doc:/streetName to "Funenpark")
     */
    private fun getResolvedValues(receiveData: List<DataBindingConfig>, data: JsonNode): Map<String, Any> {
        return receiveData.associateBy({ it.key }, { getValue(data, it.value) })
    }

    private fun getValue(data: JsonNode, path: String): Any {
        val valueNode = data.at(JsonPointer.valueOf(path))
        if (valueNode.isMissingNode) {
            throw RuntimeException("Failed to find path '$path' in data: \n${data.toPrettyString()}")
        }
        return objectMapper.treeToValue(valueNode, Object::class.java)
    }

    private fun handleTaakObjectData(
        processInstanceId: ProcessInstanceId,
        variableScope: VariableScope,
        resolvedValues: Map<String, Any>
    ) {
        if (resolvedValues.isNotEmpty()) {
            valueResolverService.handleValues(processInstanceId.toString(), variableScope, resolvedValues)
        }
    }

    private fun getVariableScope(task: CamundaTask): VariableScope {
        return runtimeService.createProcessInstanceQuery()
            .processInstanceId(task.getProcessInstanceId())
            .singleResult() as VariableScope
    }

    internal fun getDocumentenUrls(verzondenData: JsonNode): List<String> {
        val documentPathsNode = verzondenData.at(JsonPointer.valueOf("/documenten"))
        if (documentPathsNode.isMissingNode || documentPathsNode.isNull) {
            return emptyList()
        }
        if (!documentPathsNode.isArray) {
            throw NotificatiesNotificationEventException(
                "Could not retrieve document Urls.'/documenten' is not an array"
            )
        }
        val documentenUris = mutableListOf<String>()
        for (documentPathNode in documentPathsNode) {
            val documentUrlNode = verzondenData.at(JsonPointer.valueOf(documentPathNode.textValue()))
            if (!documentUrlNode.isMissingNode && !documentUrlNode.isNull) {
                try {
                    if (documentUrlNode.isTextual) {
                        documentenUris.add(documentUrlNode.textValue())
                    } else if (documentUrlNode.isArray) {
                        documentUrlNode.forEach { documentenUris.add(it.textValue()) }
                    } else {
                        throw NotificatiesNotificationEventException(
                            "Could not retrieve document Urls. Found invalid URL in '/documenten'. ${documentUrlNode.toPrettyString()}"
                        )
                    }
                } catch (e: MalformedURLException) {
                    throw NotificatiesNotificationEventException(
                        "Could not retrieve document Urls. Malformed URL in: '/documenten'"
                    )
                }
            }
        }
        return documentenUris
    }

    internal fun startProcessToUploadDocuments(
        taakObject: TaakObject,
        processDefinitionKey: String,
        businessKey: String,
        objectenApiPluginConfigurationId: String,
        portaalTaakObjectUrl: String
    ) {
        val variables = mapOf(
            "portaalTaakObjectUrl" to portaalTaakObjectUrl,
            "objectenApiPluginConfigurationId" to objectenApiPluginConfigurationId,
            "verwerkerTaakId" to taakObject.verwerkerTaakId,
            "documentUrls" to getDocumentenUrls(jacksonObjectMapper().valueToTree(taakObject.verzondenData))
        )
        try {
            runWithoutAuthorization {
                processService.startProcess(
                    processDefinitionKey,
                    businessKey,
                    variables
                )
            }
        } catch (ex: RuntimeException) {
            throw NotificatiesNotificationEventException(
                "Could not start process with definition: $processDefinitionKey and businessKey: $businessKey.\n " +
                        "Reason: ${ex.message}"
            )
        }
    }

    internal fun getPortaalTaakObjectData(
        objectManagement: ObjectManagement,
        event: NotificatiesApiNotificationReceivedEvent
    ): JsonNode {
        val objectenApiPlugin =
            pluginService
                .createInstance(PluginConfigurationId(objectManagement.objectenApiPluginConfigurationId)) as ObjectenApiPlugin
        return objectenApiPlugin.getObject(URI(event.resourceUrl)).record.data
            ?: throw NotificatiesNotificationEventException(
                "Portaaltaak meta data was empty!"
            )
    }

}
