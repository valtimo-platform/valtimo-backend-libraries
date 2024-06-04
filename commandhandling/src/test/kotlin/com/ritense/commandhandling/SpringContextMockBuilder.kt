package com.ritense.commandhandling

import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationContext

class SpringContextMockBuilder {

    fun builder(): Builder {
        return Builder()
    }

    data class Builder(
        val commandDispatcher: CommandDispatcher = mock(),
    ) {

        fun build() {
            val applicationContext: ApplicationContext = mock()
            whenever(applicationContext.getBean(CommandDispatcher::class.java)).thenReturn(commandDispatcher)
            return SpringContextHelper().setApplicationContext(applicationContext)
        }
    }

}