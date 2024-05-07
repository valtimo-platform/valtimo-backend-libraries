package com.ritense.formviewmodel.commandhandling.decorator

import com.ritense.formviewmodel.commandhandling.Command
import com.ritense.formviewmodel.commandhandling.CommandHandler

abstract class CommandHandlerDecorator<C : Command<T>, T>(
    protected val commandHandler: CommandHandler<C, T>
) : CommandHandler<C, T> by commandHandler