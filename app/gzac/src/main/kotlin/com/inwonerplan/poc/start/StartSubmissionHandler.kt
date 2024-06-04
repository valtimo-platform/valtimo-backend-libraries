package com.inwonerplan.poc.start

import com.ritense.formviewmodel.submission.FormViewModelSubmissionHandler
import com.ritense.valtimo.camunda.domain.CamundaTask

class StartSubmissionHandler : FormViewModelSubmissionHandler<StartViewModel> {

    override fun <T> handle(submission: T, task: CamundaTask?, businessKey: String) {

    }

    override fun supports(formName: String): Boolean {
        return formName == "empty-form"
    }
}