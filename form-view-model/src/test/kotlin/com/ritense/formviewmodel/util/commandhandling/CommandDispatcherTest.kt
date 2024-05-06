package com.ritense.formviewmodel.util.commandhandling

import com.ritense.formviewmodel.BaseTest
import com.ritense.valtimo.implementation.util.commandhandling.LambdaCommand
import com.ritense.valtimo.implementation.util.commandhandling.LambdaCommandHandler
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify

internal class CommandDispatcherTest : BaseTest() {

    private lateinit var commandDispatcherUnderTest: CommandDispatcher

    @BeforeEach
    fun setUp() {
        commandDispatcherUnderTest = CommandDispatcher()
    }

    @Test
    fun `should register handler`() {
        val handler = LambdaCommandHandler() as CommandHandler<Command<Unit>, Unit>

        assertThat(commandDispatcherUnderTest.commandHandlers.size).isEqualTo(0)

        commandDispatcherUnderTest.registerCommandHandler(handler)

        assertThat(commandDispatcherUnderTest.commandHandlers.size).isEqualTo(1)

        val decoratedCommandHandler = assertDoesNotThrow {
            commandDispatcherUnderTest.commandHandlers[handler.getCommandType()]!!
        }

        assertThat(decoratedCommandHandler.getCommandType()).isEqualTo(handler.getCommandType())
    }

    @Test
    fun `should dispatch and execute command`() {
        // given
        val handler = spy(LambdaCommandHandler() as CommandHandler<Command<Unit>, Unit>)
        commandDispatcherUnderTest.registerCommandHandler(handler)
        var commandTime: Long = -1
        val command = LambdaCommand {
            Thread.sleep(100)
            commandTime = System.nanoTime()
        }

        // when
        commandDispatcherUnderTest.dispatch(command)

        // then
        val afterDispatchTime = System.nanoTime()
        assertThat(commandTime).isLessThan(afterDispatchTime)
        verify(handler).execute(command)
    }

}