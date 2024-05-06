package com.ritense.formviewmodel.web.rest

import com.ritense.formviewmodel.event.OnFormSubmittedEventHandler

class TestEventHandler : OnFormSubmittedEventHandler<TestViewModel> {

    override fun supports(formName: String): Boolean {
        return formName == "formName"
    }

    override fun handle(submission: TestViewModel, taskInstanceId: String) {
        // do nothing
    }
}