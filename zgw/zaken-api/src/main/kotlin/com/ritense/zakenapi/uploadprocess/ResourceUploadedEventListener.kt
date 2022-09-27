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

package com.ritense.zakenapi.uploadprocess

import com.ritense.document.domain.impl.JsonSchemaDocumentId.existingId
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.domain.impl.request.StartProcessForDocumentRequest
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.resource.domain.MetadataType
import com.ritense.resource.domain.TemporaryResourceUploadedEvent
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.valtimo.service.CamundaTaskService
import mu.KotlinLogging
import org.camunda.bpm.engine.ActivityTypes
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.extension.reactor.bus.CamundaSelector
import org.camunda.bpm.extension.reactor.spring.listener.ReactorTaskListener
import org.springframework.context.event.EventListener
import java.util.UUID

@CamundaSelector(type = ActivityTypes.TASK_USER_TASK, event = TaskListener.EVENTNAME_COMPLETE)
open class ResourceUploadedEventListener(
    private val resourceService: TemporaryResourceStorageService,
    private val documentService: DocumentService,
    private val processDocumentService: ProcessDocumentService,
    private val documentDefinitionProcessLinkService: DocumentDefinitionProcessLinkService,
    private val runtimeService: RuntimeService,
    private val camundaTaskService: CamundaTaskService,
) : ReactorTaskListener() {

    @Synchronized
    @EventListener(TemporaryResourceUploadedEvent::class)
    fun handle(event: TemporaryResourceUploadedEvent) {
        logger.debug { "Handling TemporaryResourceUploadedEvent with resourceId: ${event.resourceId}" }

        val metadata = resourceService.getResourceMetadata(event.resourceId)
        val caseId = metadata[MetadataType.DOCUMENT_ID.key] as String?
        val taskId = metadata[MetadataType.TASK_ID.key] as String?

        if (taskId != null) {
            addResourceIdToProcessVariables(taskId, event.resourceId)
        } else if (caseId != null) {
            startUploadResourceProcesses(caseId, listOf(event.resourceId))
        }
    }

    override fun notify(delegateTask: DelegateTask) {
        val resourceIds = delegateTask.getVariable(UNIQUE_RESOURCE_IDS_PROCESS_VAR) as List<String>?
        if (!resourceIds.isNullOrEmpty()) {
            val processInstanceId = CamundaProcessInstanceId(delegateTask.processInstanceId)
            val caseId = processDocumentService.getDocumentId(processInstanceId, delegateTask).id.toString()
            startUploadResourceProcesses(caseId, resourceIds)
        }
    }

    private fun addResourceIdToProcessVariables(taskId: String, resourceId: String) {
        val executionId = camundaTaskService.findTaskById(taskId).executionId
        val resourcesIds = runtimeService.getVariable(executionId, UNIQUE_RESOURCE_IDS_PROCESS_VAR) as List<String>?
        val newResourcesIds = resourcesIds?.toMutableList()?.add(resourceId) ?: mutableListOf(resourceId)
        runtimeService.setVariable(executionId, UNIQUE_RESOURCE_IDS_PROCESS_VAR, newResourcesIds)
    }

    private fun startUploadResourceProcesses(caseId: String, resourceIds: List<String>) {
        if (assertDocumentUploadLink(caseId)) {
            resourceIds.forEach { resourceId ->
                startUploadResourceProcess(caseId, resourceId)
            }
        }
    }

    private fun assertDocumentUploadLink(caseId: String): Boolean {
        val caseDefinitionName = documentService.get(caseId).definitionId().name()
        val link = documentDefinitionProcessLinkService.getDocumentDefinitionProcessLink(caseDefinitionName)

        return if (link.isPresent && DOCUMENT_UPLOAD == link.get().type) {
            true
        } else if (link.isPresent && DOCUMENT_UPLOAD != link.get().type) {
            logger.error { "Wrong link-type found. Found ${link.get().type}, expected $DOCUMENT_UPLOAD" }
            false
        } else {
            false
        }
    }

    private fun startUploadResourceProcess(caseId: String, resourceId: String) {
        val result = processDocumentService.startProcessForDocument(
            StartProcessForDocumentRequest(
                existingId(UUID.fromString(caseId)),
                UPLOAD_DOCUMENT_PROCESS_DEFINITION_KEY,
                mapOf(RESOURCE_ID_PROCESS_VAR to resourceId)
            )
        )
        if (result.resultingDocument().isEmpty) {
            var logMessage = "Errors occurred during starting the document-upload process:"
            result.errors().forEach { logMessage += "\n - " + it.asString() }
            logger.error { logMessage }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        const val RESOURCE_ID_PROCESS_VAR = "resourceId"
        const val UNIQUE_RESOURCE_IDS_PROCESS_VAR = "resourceIds-082baa14-d0b2-4de2-80c4-d2b3565a57b9"
        const val UPLOAD_DOCUMENT_PROCESS_DEFINITION_KEY = "document-upload"
        const val DOCUMENT_UPLOAD = "DOCUMENT_UPLOAD"
    }
}
