package com.ritense.formviewmodel.commandhandling

class ExampleCommandHandler : CommandHandler<ExampleCommand, Boolean> {

    override fun execute(command: ExampleCommand): Boolean {
        // DO API CALL
        // if needed throw form exception with component key
        return true
    }

}