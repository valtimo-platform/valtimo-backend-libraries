package com.ritense.formviewmodel.event

import com.ritense.formviewmodel.commandhandling.ExampleCommand
import com.ritense.formviewmodel.commandhandling.dispatchCommand
import com.ritense.formviewmodel.viewmodel.Submission
import com.ritense.formviewmodel.viewmodel.TestViewModel

class TestEventHandler : FormViewModelSubmissionHandler<TestViewModel> {

    override fun supports(formName: String): Boolean {
        return formName == "test"
    }

    override fun handle(submission: TestViewModel) {
        val exampleCommand = ExampleCommand(
            age = submission.age!!
        )
        dispatchCommand(exampleCommand)
    }

}