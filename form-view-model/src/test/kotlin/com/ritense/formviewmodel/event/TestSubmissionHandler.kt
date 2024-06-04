package com.ritense.formviewmodel.event

import com.ritense.formviewmodel.commandhandling.ExampleCommand
import com.ritense.formviewmodel.commandhandling.dispatchCommand
import com.ritense.formviewmodel.submission.FormViewModelSubmissionHandler
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.valtimo.camunda.domain.CamundaTask

class TestSubmissionHandler : FormViewModelSubmissionHandler<TestViewModel> {

    override fun supports(formName: String): Boolean {
        return formName == "test"
    }

    override fun <T> handle(submission: T, task: CamundaTask?, businessKey: String) {
        submission as TestViewModel
        val exampleCommand = ExampleCommand(
            age = submission.age!!
        )
        dispatchCommand(exampleCommand)
    }

}