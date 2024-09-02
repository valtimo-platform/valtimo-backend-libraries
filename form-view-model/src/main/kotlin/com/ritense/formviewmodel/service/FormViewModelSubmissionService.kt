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
import com.ritense.formviewmodel.submission.FormViewModelStartFormSubmissionHandlerFactory
import com.ritense.formviewmodel.submission.FormViewModelUserTaskSubmissionHandlerFactory
import com.ritense.formviewmodel.viewmodel.Submission
import com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider.Companion.COMPLETE
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.service.CamundaTaskService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
    private val processAuthorizationService: ProcessAuthorizationService
) {

    fun handleStartFormSubmission(
        formName: String,
        processDefinitionKey: String,
        documentDefinitionName: String,
        submission: ObjectNode
    ) {
        processAuthorizationService.checkAuthorization(processDefinitionKey)
        val startFormSubmissionHandler = formViewModelStartFormSubmissionHandlerFactory.getHandler(
            formName = formName
        ) ?: throw RuntimeException("No StartFormSubmissionHandler found for formName $formName")
        val submissionType = startFormSubmissionHandler.getSubmissionType()
        val submissionConverted = parseSubmission(submission, submissionType)
        runWithoutAuthorization {
            startFormSubmissionHandler.handle(
                documentDefinitionName = documentDefinitionName,
                processDefinitionKey = processDefinitionKey,
                submission = submissionConverted
            )
        }
    }

    fun handleUserTaskSubmission(
        formName: String,
        submission: ObjectNode,
        taskInstanceId: String
    ) {
        val task = camundaTaskService.findTaskById(taskInstanceId)
        authorizationService.requirePermission(
            EntityAuthorizationRequest(CamundaTask::class.java, COMPLETE, task)
        )
        val userTaskSubmissionHandler = formViewModelUserTaskSubmissionHandlerFactory.getHandler(
            formName = formName
        ) ?: throw RuntimeException("No UserTaskSubmissionHandler found for formName $formName")
        val submissionType = userTaskSubmissionHandler.getSubmissionType()
        val submissionConverted = parseSubmission(submission, submissionType)
        runWithoutAuthorization {
            userTaskSubmissionHandler.handle(
                submission = submissionConverted,
                task = task,
                businessKey = task.processInstance!!.businessKey!!
            )
        }
    }

    private inline fun <reified T : Submission> parseSubmission(
        submission: ObjectNode,
        submissionType: KClass<out T>
    ): Submission {
        return objectMapper.convertValue(submission, submissionType.java)
    }

}