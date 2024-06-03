package com.ritense.formviewmodel.commandhandling.decorator

import com.ritense.formviewmodel.BaseTest
import com.ritense.formviewmodel.commandhandling.LambdaCommand
import com.ritense.formviewmodel.commandhandling.LambdaCommandHandler
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
            Thread.sleep(1000)
        })

        assertThat(output).contains("Timed 'LambdaCommand' execution time = ")
    }
}