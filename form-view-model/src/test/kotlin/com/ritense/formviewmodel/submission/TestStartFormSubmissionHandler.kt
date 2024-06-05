package com.ritense.formviewmodel.submission

import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.viewmodel.TestViewModel

class TestStartFormSubmissionHandler : FormViewModelStartFormSubmissionHandler<TestViewModel> {

    override fun supports(formName: String): Boolean {
        return formName == "test"
    }

    override fun <T> handle(
        documentDefinitionName: String,
        processDefinitionKey: String,
        submission: T
    ) {
        submission as TestViewModel
        if (submission.age!! < 18) {
            throw FormException("Age should be 18 or older")
        }
    }

}