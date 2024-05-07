package com.ritense.formviewmodel.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.formviewmodel.event.FormViewModelSubmission
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandlerFactory
import com.ritense.valtimo.service.CamundaTaskService

class FormViewModelSubmissionService(
    private val formViewModelSubmissionHandlerFactory: FormViewModelSubmissionHandlerFactory,
    private val camundaTaskService: CamundaTaskService
) {

    fun handleSubmission(
        formName: String,
        submission: ObjectNode,
        taskInstanceId: String
    ) {
        val formViewModelSubmissionHandler = formViewModelSubmissionHandlerFactory.getFormViewModelSubmissionHandler(
            formName = formName
        ) ?: throw RuntimeException("No event handler found for formName $formName")
        val formViewModelSubmission = FormViewModelSubmission(
            formName = formName,
            submission = submission,
            taskInstanceId = taskInstanceId,
        )
        formViewModelSubmissionHandler.handle(formViewModelSubmission)
        camundaTaskService.complete(taskInstanceId)
    }
}