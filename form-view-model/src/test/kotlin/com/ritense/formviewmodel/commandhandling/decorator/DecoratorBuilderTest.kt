package com.ritense.formviewmodel.commandhandling.decorator

import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.commandhandling.Command
import com.ritense.formviewmodel.commandhandling.CommandHandler
import com.ritense.formviewmodel.commandhandling.decorator.DecoratorBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify

internal class DecoratorBuilderTest : BaseTest() {

    @Test
    fun `should build with default decorators`() {
        val commandHandler: CommandHandler<Command<Unit>, Unit> = mock()
        val builder = spy(DecoratorBuilder(commandHandler))
        val commandHandlerDecorated = builder
            .decorateWithDefaults()
            .build()

        assertThat(commandHandlerDecorated).isNotNull
        verify(builder).decorateWithDefaults()
        verify(builder).decorateWithLogger()
        verify(builder).decorateWithExecutionTime()
    }
}