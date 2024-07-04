package com.ritense.commandhandling.decorator

import com.ritense.commandhandling.BaseTest
import com.ritense.commandhandling.LambdaCommand
import com.ritense.commandhandling.LambdaCommandHandler
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension

@ExtendWith(OutputCaptureExtension::class)
internal class ExecutionTimeDecoratorTest : BaseTest() {

    @Test
    fun `should log execution time`(output: CapturedOutput) {
        val commandHandler = ExecutionTimeDecorator(
            commandHandler = LambdaCommandHandler()
        )

        commandHandler.execute(LambdaCommand {
            Thread.sleep(100)
        })

        assertThat(output).contains("Timed 'LambdaCommand' execution time = ")
    }
}