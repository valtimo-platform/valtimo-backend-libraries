package com.ritense.formviewmodel.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.formviewmodel.event.FormViewModelSubmission
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandlerFactory
import com.ritense.formviewmodel.viewmodel.Submission
import com.ritense.valtimo.service.CamundaTaskService
import kotlin.reflect.KClass

class FormViewModelSubmissionService(
    private val formViewModelSubmissionHandlerFactory: FormViewModelSubmissionHandlerFactory,
    private val camundaTaskService: CamundaTaskService,
    private val objectMapper: ObjectMapper
) {

    fun handleSubmission(
        formName: String,
        submission: ObjectNode,
        taskInstanceId: String
    ) {
        val formViewModelSubmissionHandler = formViewModelSubmissionHandlerFactory.getFormViewModelSubmissionHandler(
            formName = formName
        ) ?: throw RuntimeException("No event handler found for formName $formName")
        val submissionType = formViewModelSubmissionHandler.getSubmissionType()
        val submissionConverted = parseSubmission(submission, submissionType)
        val formViewModelSubmission = FormViewModelSubmission(
            formName = formName,
            submission = submissionConverted,
            taskInstanceId = taskInstanceId,
        )
        formViewModelSubmissionHandler.handle(
            submission = formViewModelSubmission.submission
        )
        camundaTaskService.complete(taskInstanceId)
    }

    private inline fun <reified T : Submission> parseSubmission(
        submission: ObjectNode,
        submissionType: KClass<out T>
    ): Submission {
        return objectMapper.convertValue(submission, submissionType.java)
    }

}