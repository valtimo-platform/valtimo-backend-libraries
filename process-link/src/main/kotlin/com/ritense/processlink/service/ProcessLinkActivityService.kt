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

package com.ritense.processlink.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.AuthorizationResourceContext
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.logging.LoggableResource
import com.ritense.logging.withLoggingContext
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.exception.ProcessLinkNotFoundException
import com.ritense.processlink.web.rest.dto.ProcessLinkActivityResult
import com.ritense.processlink.web.rest.dto.ProcessLinkActivityResultWithTask
import com.ritense.valtimo.camunda.authorization.CamundaExecutionActionProvider
import com.ritense.valtimo.camunda.domain.CamundaExecution
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.Companion.byActive
import com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.Companion.byId
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.exception.ProcessDefinitionNotFoundException
import com.ritense.valtimo.service.CamundaProcessService
import com.ritense.valtimo.service.CamundaTaskService
import mu.KotlinLogging
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@SkipComponentScan
class ProcessLinkActivityService(
    private val processLinkService: ProcessLinkService,
    private val taskService: CamundaTaskService,
    private val processLinkActivityHandlers: List<ProcessLinkActivityHandler<*>>,
    private val authorizationService: AuthorizationService,
    private val camundaRepositoryService: CamundaRepositoryService,
    private val documentService: DocumentService,
    private val camundaTaskService: CamundaTaskService,
    private val camundaProcessService: CamundaProcessService,
) {
    fun openTask(
        @LoggableResource(resourceType = CamundaTask::class) taskId: UUID
    ): ProcessLinkActivityResult<*> {
        val task = try {
            taskService.findTaskOrThrow(
                byId(taskId.toString())
                    .and(byActive())
            )
        } catch (exception: Exception) {
            logger.warn("For task with id '$taskId'.", exception)
            throw ProcessLinkNotFoundException("For task with id '$taskId'.")
        }

        return processLinkService.getProcessLinks(task.getProcessDefinitionId(), task.taskDefinitionKey!!)
            .firstNotNullOfOrNull { processLink ->
                withLoggingContext(ProcessLink::class, processLink.id) {
                    processLinkActivityHandlers.firstOrNull { provider -> provider.supports(processLink) }
                        ?.openTask(task, processLink)
                }
            } ?: throw ProcessLinkNotFoundException("For task with id '$taskId'.")
    }

    fun getStartEventObject(
        @LoggableResource(resourceType = CamundaProcessDefinition::class) processDefinitionId: String,
        @LoggableResource("com.ritense.document.domain.impl.JsonSchemaDocument") documentId: UUID?,
        @LoggableResource("documentDefinitionName") documentDefinitionName: String?
    ): ProcessLinkActivityResult<*>? {
        val processLink = processLinkService.getProcessLinksByProcessDefinitionIdAndActivityType(
            processDefinitionId,
            ActivityTypeWithEventName.START_EVENT_START
        ) ?: return null

        val processDefinition = runWithoutAuthorization {
            camundaRepositoryService.findProcessDefinitionById(processLink.processDefinitionId)
                ?: throw ProcessDefinitionNotFoundException(
                    "For process definition with id ${processLink.processDefinitionId}"
                )
        }

        var entityAuthorizationRequest = EntityAuthorizationRequest(
            CamundaExecution::class.java,
            CamundaExecutionActionProvider.CREATE,
            createDummyCamundaExecution(
                processDefinition
            )
        )

        documentId?.let {
            entityAuthorizationRequest = entityAuthorizationRequest.withContext(
                AuthorizationResourceContext(
                    JsonSchemaDocument::class.java,
                    documentService.findBy(JsonSchemaDocumentId.existingId(documentId)).get() as JsonSchemaDocument
                )
            )
        }

        authorizationService.requirePermission(entityAuthorizationRequest)
        return withLoggingContext(ProcessLink::class, processLink.id) {
            processLinkActivityHandlers
                .find { it.supports(processLink) }
                ?.getStartEventObject(processDefinitionId, documentId, documentDefinitionName, processLink)
        }
    }

    fun getTasksWithProcessLinks(processInstanceId: String): List<ProcessLinkActivityResultWithTask> {
        val tasks = runWithoutAuthorization {
            camundaProcessService.findProcessInstanceById(processInstanceId)
        }.let { processInstance ->
            processInstance.orElse(null)?.let {
                camundaTaskService.getProcessInstanceTasks(it.id, it.businessKey)
            } ?: emptyList()
        }

        return tasks.map { task ->
            try {
                val activityResult = openTask(UUID.fromString(task.taskDto.id))
                ProcessLinkActivityResultWithTask(task, activityResult)
            } catch (e: ProcessLinkNotFoundException) {
                ProcessLinkActivityResultWithTask(task, null)
            }
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}

        fun createDummyCamundaExecution(
            processDefinition: CamundaProcessDefinition,
            businessKey: String? = null
        ): CamundaExecution {
            val execution = CamundaExecution(
                UUID.randomUUID().toString(),
                1,
                null,
                null,
                businessKey,
                null,
                processDefinition,
                null,
                null,
                null,
                null,
                null,
                true,
                false,
                false,
                false,
                SuspensionState.ACTIVE.stateCode,
                0,
                0,
                null,
                HashSet()
            )
            execution.processInstance = execution
            return execution
        }

    }
}
