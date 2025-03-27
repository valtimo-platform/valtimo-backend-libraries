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

package com.ritense.formviewmodel.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.formviewmodel.submission.FormViewModelStartFormSubmissionHandlerFactory
import com.ritense.formviewmodel.submission.FormViewModelUserTaskSubmissionHandlerFactory
import com.ritense.formviewmodel.viewmodel.Submission
import com.ritense.formviewmodel.web.rest.dto.StartFormSubmissionResult
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider.Companion.COMPLETE
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.service.CamundaTaskService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.reflect.KClass

@Transactional
@Service
@SkipComponentScan
class FormViewModelSubmissionService(
    private val formViewModelStartFormSubmissionHandlerFactory: FormViewModelStartFormSubmissionHandlerFactory,
    private val formViewModelUserTaskSubmissionHandlerFactory: FormViewModelUserTaskSubmissionHandlerFactory,
    private val authorizationService: AuthorizationService,
    private val camundaTaskService: CamundaTaskService,
    private val objectMapper: ObjectMapper,
    private val processAuthorizationService: ProcessAuthorizationService,
    private val processLinkService: ProcessLinkService,
    private val documentService: JsonSchemaDocumentService,
) {

    @Deprecated("Deprecated since 12.6.0", replaceWith = ReplaceWith("handleStartFormSubmission(processDefinitionKey, documentDefinitionName, submission)"))
    fun handleStartFormSubmission(
        formName: String,
        processDefinitionKey: String,
        documentDefinitionName: String,
        submission: ObjectNode
    ) = handleStartFormSubmission(processDefinitionKey, documentDefinitionName, submission)

    fun handleStartFormSubmission(
        processDefinitionKey: String,
        documentDefinitionName: String,
        submission: ObjectNode,
        documentId: UUID? = null,
    ): StartFormSubmissionResult {
        val document = documentId?.let {
            runWithoutAuthorization {
                documentService.getDocumentBy(JsonSchemaDocumentId.existingId(documentId))
            }
        }

        processAuthorizationService.checkStartProcessAuthorization(processDefinitionKey, document)

        val processLink = runWithoutAuthorization {
            getStartEventProcessLink(processDefinitionKey)
                ?: throw RuntimeException("No start event process link found for processDefinitionKey=$processDefinitionKey")
        }

        val startFormSubmissionHandler = formViewModelStartFormSubmissionHandlerFactory.getHandler(
            processLink
        ) ?: throw RuntimeException("No StartFormSubmissionHandler found for processDefinitionKey=$processDefinitionKey and processLink=${processLink.id}")
        val submissionType = startFormSubmissionHandler.getSubmissionType()

        val submissionConverted = parseSubmission(submission, submissionType)

        return startFormSubmissionHandler.handle(
            documentDefinitionName = documentDefinitionName,
            processDefinitionKey = processDefinitionKey,
            submission = submissionConverted,
            document = document,
        )
    }

    @Deprecated("Deprecated since 12.6.0", replaceWith = ReplaceWith("handleUserTaskSubmission(submission, taskInstanceId)"))
    fun handleUserTaskSubmission(
        formName: String,
        submission: ObjectNode,
        taskInstanceId: String
    ) = handleUserTaskSubmission(submission, taskInstanceId)

    fun handleUserTaskSubmission(
        submission: ObjectNode,
        taskInstanceId: String
    ) {
        val task = camundaTaskService.findTaskById(taskInstanceId)
        authorizationService.requirePermission(
            EntityAuthorizationRequest(CamundaTask::class.java, COMPLETE, task)
        )

        val processLink = runWithoutAuthorization {
            getUserTaskProcessLink(task)
                ?: throw RuntimeException("No process link found for task ${task.processDefinition?.key}:${task.taskDefinitionKey}:${task.id}")
        }

        val userTaskSubmissionHandler = formViewModelUserTaskSubmissionHandlerFactory.getHandler(processLink
        ) ?: throw RuntimeException("No UserTaskSubmissionHandler found for task=${task.processDefinition?.key}:${task.taskDefinitionKey}:${task.id} and processLink=${processLink.id}")
        val submissionType = userTaskSubmissionHandler.getSubmissionType()
        val submissionConverted = parseSubmission(submission, submissionType)

        userTaskSubmissionHandler.handle(
            submission = submissionConverted,
            task = task,
            businessKey = task.processInstance!!.businessKey!!
        )
    }

    private inline fun <reified T : Submission> parseSubmission(
        submission: ObjectNode,
        submissionType: KClass<out T>
    ): Submission {
        return objectMapper.convertValue(submission, submissionType.java)
    }

    private fun getStartEventProcessLink(processDefinitionKey: String) = processLinkService.getProcessLinksByProcessDefinitionKey(
        processDefinitionKey
    ).firstOrNull { it.activityType === ActivityTypeWithEventName.START_EVENT_START }

    private fun getUserTaskProcessLink(task: CamundaTask): ProcessLink? {
        return task.processDefinition?.let { processDefinition ->
            processLinkService.getProcessLinksByProcessDefinitionKey(
                processDefinition.key,
            ).firstOrNull { it.activityType == ActivityTypeWithEventName.USER_TASK_CREATE && it.activityId == task.taskDefinitionKey }
        }
    }
}