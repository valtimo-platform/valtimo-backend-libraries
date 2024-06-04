package com.ritense.formviewmodel.event

import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.submission.FormViewModelSubmissionHandler
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.valtimo.camunda.domain.CamundaTask

class TestSubmissionHandler : FormViewModelSubmissionHandler<TestViewModel> {

    override fun supports(formName: String): Boolean {
        return formName == "test"
    }

    override fun <T> handle(submission: T, task: CamundaTask?, businessKey: String) {
        submission as TestViewModel
        if (submission.age!! < 18) {
            throw FormException("Age should be 18 or older")
        }
    }

}