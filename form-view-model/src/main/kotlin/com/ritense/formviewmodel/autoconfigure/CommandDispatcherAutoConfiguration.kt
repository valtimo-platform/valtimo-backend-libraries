package com.ritense.formviewmodel.autoconfigure

import com.ritense.valtimo.implementation.util.commandhandling.Command
import com.ritense.valtimo.implementation.util.commandhandling.CommandDispatcher
import com.ritense.valtimo.implementation.util.commandhandling.CommandHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommandDispatcherAutoConfiguration {

    @Bean
    fun commandDispatcher(handlers: List<CommandHandler<*, *>>): CommandDispatcher {
        return CommandDispatcher().apply {
            handlers.map { this.registerCommandHandler(it as CommandHandler<Command<Any>, Any>) }
        }
    }

}