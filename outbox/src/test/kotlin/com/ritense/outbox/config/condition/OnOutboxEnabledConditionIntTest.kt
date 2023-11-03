package com.ritense.outbox.config.condition

import com.ritense.outbox.OutboxService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@Tag("integration")
class OnOutboxEnabledConditionIntTest {

    @Nested
    @SpringBootTest(properties = ["${OnOutboxEnabledCondition.PROPERTY_NAME}=true"])
    inner class Enabled @Autowired constructor(
        private val context: ApplicationContext
    ) {
        @Test
        fun `Should create an OutboxService bean`() {
            val bean = context.getBean(OutboxService::class.java)
            Assertions.assertThat(bean).isNotNull
        }
    }

    @Nested
    @SpringBootTest(properties = ["${OnOutboxEnabledCondition.PROPERTY_NAME}=false"])
    inner class Disabled @Autowired constructor(
        private val context: ApplicationContext
    ) {
        @Test
        fun `Should not create an OutboxService bean`() {
            assertThrows<NoSuchBeanDefinitionException> {
                context.getBean(OutboxService::class.java)
            }
        }
    }
}