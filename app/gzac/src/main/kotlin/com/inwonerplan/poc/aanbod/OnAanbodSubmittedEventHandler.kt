package com.inwonerplan.poc.aanbod

import com.ritense.formviewmodel.event.FormViewModelSubmissionHandler
import com.ritense.valtimo.camunda.domain.CamundaTask


class OnAanbodSubmittedEventHandler : FormViewModelSubmissionHandler<AanbodViewModel> {

    override fun supports(formName: String): Boolean {
        return formName == "form_aanbod"
    }

    override fun <T> handle(submission: T, task: CamundaTask) {
        println(submission)
    }
}