package com.ritense.commandhandling.decorator

import com.ritense.commandhandling.BaseTest
import com.ritense.commandhandling.LambdaCommand
import com.ritense.commandhandling.LambdaCommandHandler
import nl.altindag.log.LogCaptor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.OutputCaptureExtension

@ExtendWith(OutputCaptureExtension::class)
internal class LogDecoratorTest : BaseTest() {

    @Test
    fun `should log command`() {
        val logCaptor = LogCaptor.forClass(LogDecorator::class.java)

        val commandHandler = LogDecorator(
            commandHandler = LambdaCommandHandler()
        )

        commandHandler.execute(LambdaCommand {
            Thread.sleep(100)
        })

        assertThat(logCaptor.infoLogs).anyMatch { it.contains("Handler 'LambdaCommandHandler' executing 'LambdaCommand'") }
        assertThat(logCaptor.traceLogs).anyMatch { it.contains("Command details 'LambdaCommand(lambda=com.ritense.commandhandling.decorator.LogDecoratorTest\$\$Lambda") }
    }
}