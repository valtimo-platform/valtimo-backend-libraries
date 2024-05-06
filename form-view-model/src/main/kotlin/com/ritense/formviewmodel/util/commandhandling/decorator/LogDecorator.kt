package com.ritense.formviewmodel.util.commandhandling.decorator

import com.ritense.formviewmodel.util.commandhandling.Command
import com.ritense.formviewmodel.util.commandhandling.CommandHandler
import mu.KotlinLogging

class LogDecorator<C : Command<T>, T>(
    commandHandler: CommandHandler<C, T>
) : CommandHandlerDecorator<C, T>(commandHandler) {

    override fun execute(command: C) : T {
        logger.info { constructInfoLogMessage(command) }
        logger.trace { "Command details '$command'" }
        return super.execute(command)
    }

    private fun constructInfoLogMessage(command: C): String {
        return "Handler '${commandHandler::class.simpleName}' executing '${command::class.simpleName}'"
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}