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
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.document.domain.impl.request.ModifyDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.notificatiesapi.event.NotificatiesApiNotificationReceivedEvent
import com.ritense.notificatiesapi.exception.NotificatiesException
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.service.ObjectManagementService
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.service.CamundaProcessService
import java.net.MalformedURLException
import java.net.URI
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity
import org.camunda.bpm.engine.task.Task
import org.springframework.context.event.EventListener
import org.springframework.http.HttpStatus

class PortaalTaakEventListener(
    private val objectManagementService: ObjectManagementService,
    private val pluginService: PluginService,
    private val processDocumentService: ProcessDocumentService,
    private val processService: CamundaProcessService,
    private val taskService: TaskService,
    private val documentService: DocumentService
) {

    @EventListener(NotificatiesApiNotificationReceivedEvent::class)
    fun processCompletePortaalTaakEvent(event: NotificatiesApiNotificationReceivedEvent) {
        val objectType = event.kenmerken["objectType"]

        if (!event.kanaal.equals("objecten", ignoreCase = true) ||
            !event.actie.equals("update", ignoreCase = true) ||
            objectType == null
        ) {
            return
        }

        val objectManagement =
            objectManagementService.findByObjectTypeId(objectType.substringAfterLast("/")) ?: return

        // todo need clarification why this is really needed
        pluginService.createInstance(PortaaltaakPlugin::class.java) { properties: JsonNode
            ->
            properties.get("objectManagementConfigurationId").textValue().equals(objectManagement.id.toString())
        }?.run {
            val taakObject: TaakObject =
                jacksonObjectMapper().convertValue(getPortaalTaakObjectData(objectManagement, event))
            when (taakObject.status) {
                TaakStatus.INGEDIEND -> {
                    val task = taskService.createTaskQuery().taskId(taakObject.verwerkerTaakId).singleResult()
                    saveDataInDocument(taakObject, task.processInstanceId, task)
                    startProcessToUploadDocuments(
                        taakObject,
                        this.uploadedDocumentsHandlerProcess,
                        task.processInstanceId,
                        objectManagement.objectenApiPluginConfigurationId.toString(),
                        event.resourceUrl
                    )
                }

                else -> throw NotificatiesException("", HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    internal fun saveDataInDocument(taakObject: TaakObject, processInstanceId: String, task: Task) {
        val document =
            processDocumentService.getDocument(CamundaProcessInstanceId(processInstanceId), task as TaskEntity)
        documentService.modifyDocument(
            ModifyDocumentRequest(
                document.id().toString(),
                jacksonObjectMapper().valueToTree(taakObject.verzondenData),
                document.version().toString()
            )
        ).also { result ->
            if (result.errors().size > 0) {
                throw NotificatiesException(
                    "Could not update document" +
                            "Reason:\n" +
                            result.errors().joinToString(separator = "\n - "),
                    HttpStatus.INTERNAL_SERVER_ERROR
                )
            }
        }
    }

    internal fun getDocumentenUrls(verzondenData: JsonNode): List<String> {
        if (verzondenData.isMissingNode || verzondenData.isNull) {
            return emptyList()
        }
        if (!verzondenData.isArray) {
            throw RuntimeException("Not an array: '/documenten'")
        }
        val documentenUris = mutableListOf<String>()
        for (documentPathNode in verzondenData) {
            val documentUrlNode = verzondenData.at(JsonPointer.valueOf(documentPathNode.textValue()))
            if (!documentUrlNode.isMissingNode && !documentUrlNode.isNull) {
                try {
                    if (documentUrlNode.isTextual) {
                        documentenUris.add(documentUrlNode.textValue())
                    } else if (documentUrlNode.isArray) {
                        documentUrlNode.forEach { documentenUris.add(it.textValue()) }
                    } else {
                        throw RuntimeException("Invalid URL in '/documenten'. ${documentUrlNode.toPrettyString()}")
                    }
                } catch (e: MalformedURLException) {
                    throw RuntimeException("Malformed URL in: '/documenten'", e)
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
        portaalTaakObjectResourceUrl: String
    ) {
        val variables = mapOf(
            "portaalTaakObjectResourceUrl" to portaalTaakObjectResourceUrl,
            "objectenApiPluginConfigurationId" to objectenApiPluginConfigurationId,
            "verwerkerTaakId" to taakObject.verwerkerTaakId,
            "documentUrls" to getDocumentenUrls(jacksonObjectMapper().valueToTree(taakObject.verzondenData))
        )
        try {
            processService.startProcess(
                processDefinitionKey,
                businessKey,
                variables
            )
        } catch (ex: RuntimeException) {
            throw NotificatiesException(
                "Could not start process with definition: $processDefinitionKey and businessKey: $businessKey.\n " +
                        "Reason: ${ex.message}",
                HttpStatus.INTERNAL_SERVER_ERROR
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
            ?: throw NotificatiesException("Portaaltaak meta data was empty!", HttpStatus.INTERNAL_SERVER_ERROR)
    }

}