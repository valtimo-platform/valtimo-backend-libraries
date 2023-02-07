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
    fun somthing(event: NotificatiesApiNotificationReceivedEvent) {
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
                        task.processInstanceId
                    )
                }

                else -> throw NotificatiesException("", HttpStatus.INTERNAL_SERVER_ERROR)
            }

            //todo create service to Complete the camunda task
        }
    }

    private fun saveDataInDocument(taakObject: TaakObject, processInstanceId: String, task: Task) {
        val document =
            processDocumentService.getDocument(CamundaProcessInstanceId(processInstanceId), task as TaskEntity)
        val result = documentService.modifyDocument(
            ModifyDocumentRequest(
                document.id().toString(),
                jacksonObjectMapper().valueToTree(taakObject.verzondenData),
                document.version().toString()
            )
        )
        //evaluate result
        TODO("Not yet implemented")
    }

    private fun getDocumentenUrls(verzondenData: Map<String, Any>?): List<String> {
        TODO("Not yet implemented")
    }

    private fun startProcessToUploadDocuments(
        taakObject: TaakObject,
        processDefinitionKey: String,
        businessKey: String
    ) {
        val variables = mapOf<String, Any>(
            "verwerkerTaakId" to taakObject.verwerkerTaakId,
            "documentUrls" to getDocumentenUrls(taakObject.verzondenData)
        )
        //todo start process this.uploadedDocumentsHandlerProcess
        processService.startProcess(
            processDefinitionKey,
            businessKey,
            variables
        )
        TODO("Not yet implemented")
    }

    private fun getPortaalTaakObjectData(
        objectManagement: ObjectManagement,
        event: NotificatiesApiNotificationReceivedEvent
    ): JsonNode {
        val objectenApiPlugin =
            pluginService
                .createInstance(PluginConfigurationId(objectManagement.objectenApiPluginConfigurationId)) as ObjectenApiPlugin
        val portaalTaakObjectData = objectenApiPlugin.getObject(URI(event.resourceUrl)).record.data
            ?: throw NotificatiesException("Portaaltaak meta data was empty!", HttpStatus.INTERNAL_SERVER_ERROR)
        return portaalTaakObjectData
    }

}