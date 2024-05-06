package com.ritense.formviewmodel.util.commandhandling.decorator

import com.ritense.formviewmodel.util.commandhandling.Command
import com.ritense.formviewmodel.util.commandhandling.CommandHandler
import mu.KotlinLogging

class ExecutionTimeDecorator<C : Command<T>, T>(
    commandHandler: CommandHandler<C, T>
) : CommandHandlerDecorator<C, T>(commandHandler) {

    override fun execute(command: C): T {
        val startTime = System.currentTimeMillis()
        val result = super.execute(command)
        val endTime = System.currentTimeMillis()
        val totalTime = endTime - startTime
        logger.trace { "Timed '${command.javaClass.simpleName}' execution time = '$totalTime' in milliseconds" }
        return result
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }

}