package com.ritense.valtimo.implementation.util.commandhandling

import com.ritense.formviewmodel.util.commandhandling.CommandHandler

class LambdaCommandHandler : CommandHandler<LambdaCommand, Unit> {

    override fun execute(command: LambdaCommand) = command.lambda.run()

}