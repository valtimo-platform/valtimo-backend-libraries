package com.ritense.formviewmodel.util.commandhandling.decorator

import com.ritense.formviewmodel.util.commandhandling.Command
import com.ritense.formviewmodel.util.commandhandling.CommandHandler

abstract class CommandHandlerDecorator<C : Command<T>, T>(
    protected val commandHandler: CommandHandler<C, T>
) : CommandHandler<C, T> by commandHandler