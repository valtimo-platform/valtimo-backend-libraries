package com.ritense.commandhandling

class ExampleCommandHandler : CommandHandler<ExampleCommand, Unit> {

    override fun execute(command: ExampleCommand) {
        // DO API CALL
        // if needed throw form exception with component key
        return
    }

}