package com.ritense.formviewmodel.util.commandhandling

import com.ritense.formviewmodel.util.SpringContextHelper
import com.ritense.formviewmodel.util.commandhandling.decorator.DecoratorBuilder
import mu.KotlinLogging
import kotlin.reflect.KClass

class CommandDispatcher {

    val commandHandlers = mutableMapOf<KClass<*>, CommandHandler<Command<*>, *>>()

    fun <T> dispatch(command: Command<T>) : T {
        logger.info { "Dispatching command '${command.javaClass.simpleName}'" }
        try {
            val commandClass = command::class as KClass<*>
            if (!commandHandlers.containsKey(commandClass)) {
                throw NoHandlerForCommandException(command)
            }
            @Suppress("UNCHECKED_CAST")
            return commandHandlers[commandClass]!!.execute(command) as T
        } catch (ex: Exception) {
            logger.error(ex) {
                "Unhandled Command error occurred in ${command.javaClass.simpleName} - ${ex.message} - ${ex.cause}"
            }
            throw ex
        }
    }

    fun <T> registerCommandHandler(commandHandler: CommandHandler<Command<T>, T>) {
        @Suppress("UNCHECKED_CAST")
        commandHandlers[commandHandler.getCommandType()] =
            DecoratorBuilder(commandHandler)
                .decorateWithDefaults()
                .build() as CommandHandler<Command<*>, *>
    }

    private val logger = KotlinLogging.logger {}

}

fun <T> dispatchCommand(command: Command<T>) =
    SpringContextHelper.getBean(CommandDispatcher::class.java).dispatch(command)