package com.ritense.formviewmodel.event

import com.ritense.formviewmodel.commandhandling.ExampleCommand
import com.ritense.formviewmodel.commandhandling.dispatchCommand

class TestEventHandler : FormViewModelSubmissionHandler {

    override fun supports(formName: String): Boolean {
        return formName == "test"
    }

    override fun handle(formViewModelSubmission: FormViewModelSubmission) {
        println("Handling submission")
        // Validate command
        val exampleCommand = ExampleCommand(
            age = formViewModelSubmission.submission["age"].asInt()
        )
        // dispatch command
        dispatchCommand(exampleCommand)
    }

}