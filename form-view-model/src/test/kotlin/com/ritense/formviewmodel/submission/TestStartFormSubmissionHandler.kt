package com.ritense.formviewmodel.submission

import com.ritense.formviewmodel.viewmodel.TestViewModel

class TestStartFormSubmissionHandler : FormViewModelStartFormSubmissionHandler<TestViewModel> {

    override fun supports(formName: String): Boolean {
        return formName == "test"
    }

    override fun <T> handle(
        documentDefinitionName: String,
        processDefinitionKey: String,
        businessKey: String,
        submission: T
    ) {
        submission as TestViewModel

        // Dispatch the StartProcessCommand here
    }

}