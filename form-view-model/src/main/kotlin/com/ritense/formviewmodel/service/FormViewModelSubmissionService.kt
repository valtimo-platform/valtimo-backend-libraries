package com.ritense.formviewmodel.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.formviewmodel.event.FormViewModelSubmission
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandler
import com.ritense.valtimo.service.CamundaTaskService

class FormViewModelSubmissionService(
    private val formViewModelSubmissionHandlers: List<FormViewModelSubmissionHandler>,
    private val camundaTaskService: CamundaTaskService,
) {

    fun handleSubmission(
        formName: String,
        submission: ObjectNode,
        taskInstanceId: String
    ) {
        val formViewModelSubmissionHandler = formViewModelSubmissionHandlers.find { it.supports(formName) }
            ?: throw RuntimeException("No event handler found for formName $formName")
        val formViewModelSubmission = FormViewModelSubmission(
            formName = formName,
            submission = submission,
            taskInstanceId = taskInstanceId,
        )
        formViewModelSubmissionHandler.handle(formViewModelSubmission)
        camundaTaskService.complete(taskInstanceId)
    }
}