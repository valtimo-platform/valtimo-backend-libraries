package com.ritense.valtimo.implementation.util.commandhandling.decorator

import com.ritense.valtimo.implementation.util.commandhandling.Command
import com.ritense.valtimo.implementation.util.commandhandling.CommandHandler

abstract class CommandHandlerDecorator<C : Command<T>, T>(
    protected val commandHandler: CommandHandler<C, T>
) : CommandHandler<C, T> by commandHandler