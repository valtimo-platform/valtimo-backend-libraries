package com.ritense.formviewmodel.commandhandling

import com.ritense.formviewmodel.commandhandling.CommandHandler

import com.ritense.formviewmodel.util.commandhandling.CommandHandler

class LambdaCommandHandler : CommandHandler<LambdaCommand, Unit> {

    override fun execute(command: LambdaCommand) = command.lambda.run()

}