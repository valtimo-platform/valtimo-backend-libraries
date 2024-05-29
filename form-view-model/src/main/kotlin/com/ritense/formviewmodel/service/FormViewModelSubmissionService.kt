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
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandlerFactory
import com.ritense.formviewmodel.viewmodel.Submission
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.service.CamundaProcessService
import com.ritense.valtimo.service.CamundaTaskService
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Transactional
class FormViewModelSubmissionService(
    private val formViewModelSubmissionHandlerFactory: FormViewModelSubmissionHandlerFactory,
    private val camundaTaskService: CamundaTaskService,
    private val camundaProcessService: CamundaProcessService,
    private val objectMapper: ObjectMapper
) {

    fun handleStartFormSubmission(
        formName: String,
        processDefinitionKey: String,
        businessKey: String,
        submission: ObjectNode
    ) {
        val formViewModelSubmissionHandler = formViewModelSubmissionHandlerFactory.getFormViewModelSubmissionHandler(
            formName = formName
        ) ?: throw RuntimeException("No FormViewModelSubmissionHandler found for formName $formName")
        val submissionType = formViewModelSubmissionHandler.getSubmissionType()
        val submissionConverted = parseSubmission(submission, submissionType)
        formViewModelSubmissionHandler.handle(
            submission = submissionConverted,
        )
        camundaProcessService.startProcess(
            processDefinitionKey,
            businessKey,
            emptyMap()
        )
    }

    fun handleUserTaskSubmission(
        formName: String,
        submission: ObjectNode,
        task: CamundaTask
    ) {
        val formViewModelSubmissionHandler = formViewModelSubmissionHandlerFactory.getFormViewModelSubmissionHandler(
            formName = formName
        ) ?: throw RuntimeException("No FormViewModelSubmissionHandler found for formName $formName")
        val submissionType = formViewModelSubmissionHandler.getSubmissionType()
        val submissionConverted = parseSubmission(submission, submissionType)
        formViewModelSubmissionHandler.handle(
            submission = submissionConverted,
            task = task
        )
        camundaTaskService.complete(task.id)
    }

    private inline fun <reified T : Submission> parseSubmission(
        submission: ObjectNode,
        submissionType: KClass<out T>
    ): Submission {
        return objectMapper.convertValue(submission, submissionType.java)
    }

}