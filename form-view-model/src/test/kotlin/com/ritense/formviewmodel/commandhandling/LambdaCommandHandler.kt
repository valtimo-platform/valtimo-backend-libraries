package com.ritense.formviewmodel.commandhandling

class LambdaCommandHandler : CommandHandler<LambdaCommand, Unit> {

    override fun execute(command: LambdaCommand) = command.lambda.run()

}