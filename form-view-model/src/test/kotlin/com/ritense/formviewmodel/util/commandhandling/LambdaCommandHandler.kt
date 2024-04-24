package com.ritense.valtimo.implementation.util.commandhandling

class LambdaCommandHandler : CommandHandler<LambdaCommand, Unit> {

    override fun execute(command: LambdaCommand) = command.lambda.run()

}