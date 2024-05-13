package com.ritense.formviewmodel.event

import com.ritense.formviewmodel.commandhandling.ExampleCommand
import com.ritense.formviewmodel.commandhandling.dispatchCommand
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.valtimo.camunda.domain.CamundaTask

class TestSubmissionHandler : FormViewModelSubmissionHandler<TestViewModel> {

    override fun supports(formName: String): Boolean {
        return formName == "test"
    }

    override fun <T> handle(submission: T, task: CamundaTask) {
        submission as TestViewModel

        // Documentation : a submission is a data class can be the same ViewModel
        // Submission validate structure
        // Can have validation on values of the submission, age must be over 18.
        //  - Ideally the update of the VM is checking this also.
        //  - Additional front-end validation can take place, but we want also BE checks
        // TODO : how to handle validation errors in a generic way?
        // Should communicate that command did not work!
        // Command error are not use-full for a FE user this needs to go to Support.
        // A command can have additional Business logic. How to handle this?
        //  - A command can have exceptions
        //  - A handler can throw runtime exceptions -> these are translated to a Generic error above the Form,
        //      - no component relation unless you specify this in the error message mapping the component key.
        val exampleCommand = ExampleCommand( // Can also throw FormException on runtime otherwise top level error, Developer discretion
            age = submission.age!!
        )
        dispatchCommand(exampleCommand) // Can also throw FormException on runtime otherwise top level error, Developer discretion
    }

}