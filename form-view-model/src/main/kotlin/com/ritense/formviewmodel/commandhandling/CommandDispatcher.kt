/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.formviewmodel.commandhandling

import com.ritense.formviewmodel.SpringContextHelper
import com.ritense.formviewmodel.commandhandling.decorator.DecoratorBuilder
import mu.KotlinLogging
import java.util.NoSuchElementException
import kotlin.reflect.KClass

class CommandDispatcher {

    val commandHandlers = mutableMapOf<KClass<*>, CommandHandler<Command<*>, *>>()

    fun <T> dispatch(command: Command<T>): T {
        logger.info { "Dispatching command '${command.javaClass.simpleName}'" }
        try {
            val commandClass = command::class as KClass<*>
            if (!commandHandlers.containsKey(commandClass)) {
                throw NoHandlerForCommandException(command)
            }
            @Suppress("UNCHECKED_CAST")
            return commandHandlers[commandClass]?.let {
                it.execute(command) as T
            } ?: throw NoSuchElementException("No CommandHandler found for $command")
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

fun <T> dispatchCommands(commands: List<Command<T>>) = commands.forEach { dispatchCommand(it) }