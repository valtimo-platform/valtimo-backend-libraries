package com.ritense.formviewmodel.web.rest

import com.ritense.formviewmodel.event.OnFormSubmittedEventHandler

class TestEventHandler : OnFormSubmittedEventHandler<TestViewModel> {

    override fun supports(formId: String): Boolean {
        return formId == "formId"
    }

    override fun handle(submission: TestViewModel, taskInstanceId: String) {
        // do nothing
    }
}