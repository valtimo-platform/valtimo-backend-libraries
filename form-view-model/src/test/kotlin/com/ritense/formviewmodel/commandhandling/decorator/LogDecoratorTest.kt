package com.ritense.formviewmodel.commandhandling.decorator

import ch.qos.logback.classic.Level
import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.commandhandling.Command
import com.ritense.formviewmodel.commandhandling.CommandHandler
import com.ritense.formviewmodel.commandhandling.LambdaCommand
import com.ritense.formviewmodel.commandhandling.LambdaCommandHandler
import com.ritense.formviewmodel.commandhandling.decorator.DecoratorBuilder
import mu.KotlinLogging.logger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension

@ExtendWith(OutputCaptureExtension::class)
internal class LogDecoratorTest : BaseTest() {

    @Test
    fun `should log command`(output: CapturedOutput) {
        val commandHandler = LogDecorator(
            commandHandler = LambdaCommandHandler()
        )

        commandHandler.execute(LambdaCommand {
            Thread.sleep(1000)
        })

        assertThat(output).contains("Handler 'LambdaCommandHandler' executing 'LambdaCommand'")
        assertThat(output).contains("Command details 'LambdaCommand(lambda=com.ritense.formviewmodel.commandhandling.decorator.LogDecoratorTest\$\$Lambda")
    }
}