package com.inwonerplan.poc.aanbod

import com.inwonerplan.poc.aanbod.command.SaveAanbodSubmissionCommand
import com.ritense.formviewmodel.commandhandling.dispatchCommand
import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandler
import com.ritense.valtimo.camunda.domain.CamundaTask

class OnAanbodSubmittedEventHandler : FormViewModelSubmissionHandler<AanbodSubmission> {

    override fun <T> handle(submission: T, task: CamundaTask) {
        dispatchCommand(SaveAanbodSubmissionCommand(submission as AanbodSubmission, task))
    }

    override fun supports(formName: String): Boolean {
        return formName == "form_aanbod"
    }
}