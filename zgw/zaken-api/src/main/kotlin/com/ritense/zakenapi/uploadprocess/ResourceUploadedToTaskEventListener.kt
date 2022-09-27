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

import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
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

@CamundaSelector(type = ActivityTypes.TASK_USER_TASK, event = TaskListener.EVENTNAME_COMPLETE)
open class ResourceUploadedToTaskEventListener(
    private val resourceService: TemporaryResourceStorageService,
    private val processDocumentService: ProcessDocumentService,
    private val runtimeService: RuntimeService,
    private val camundaTaskService: CamundaTaskService,
    private val uploadProcessService: UploadProcessService,
) : ReactorTaskListener() {

    @Synchronized
    @EventListener(TemporaryResourceUploadedEvent::class)
    fun handle(event: TemporaryResourceUploadedEvent) {
        logger.debug { "Handling TemporaryResourceUploadedEvent with resourceId: ${event.resourceId}" }

        val metadata = resourceService.getResourceMetadata(event.resourceId)
        val taskId = metadata[MetadataType.TASK_ID.key] as String?

        if (taskId != null) {
            addResourceIdToProcessVariables(taskId, event.resourceId)
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
        if (uploadProcessService.assertDocumentUploadLink(caseId)) {
            logger.debug { "Uploading resources to document: ${resourceIds.size}" }
            resourceIds.forEach { resourceId ->
                uploadProcessService.startUploadResourceProcess(caseId, resourceId)
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        const val UNIQUE_RESOURCE_IDS_PROCESS_VAR = "resourceIds-082baa14-d0b2-4de2-80c4-d2b3565a57b9"
    }
}
