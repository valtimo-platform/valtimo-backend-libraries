package com.ritense.valtimo.implementation.util.commandhandling.decorator

import com.ritense.valtimo.implementation.util.commandhandling.Command
import com.ritense.valtimo.implementation.util.commandhandling.CommandHandler
import mu.KotlinLogging

class DecoratorBuilder<T>(private val handler: CommandHandler<Command<T>, T>) {
    private var decoratedHandler = handler

    fun decorateWithDefaults() = decorateWithLogger()
        .decorateWithExecutionTime()

    fun decorateWithLogger(): DecoratorBuilder<T> {
        decoratedHandler = LogDecorator(decoratedHandler)
        logger.debug { "Decorated '${handler.javaClass.simpleName}' with LogDecorator" }
        return this
    }

    fun decorateWithExecutionTime(): DecoratorBuilder<T> {
        decoratedHandler = ExecutionTimeDecorator(decoratedHandler)
        logger.debug { "Decorated '${handler.javaClass.simpleName}' with ExecutionTimeDecorator" }
        return this
    }

    fun build() = decoratedHandler

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}