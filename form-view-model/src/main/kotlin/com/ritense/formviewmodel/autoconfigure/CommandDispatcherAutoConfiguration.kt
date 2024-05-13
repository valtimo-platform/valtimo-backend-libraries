package com.ritense.formviewmodel.autoconfigure

import com.ritense.formviewmodel.commandhandling.Command
import com.ritense.formviewmodel.commandhandling.CommandDispatcher
import com.ritense.formviewmodel.commandhandling.CommandHandler
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