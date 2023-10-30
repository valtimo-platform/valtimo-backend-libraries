package com.ritense.outbox.rabbitmq

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate

class RabbitMessagePublisherTest {

    @Test
    fun `initialization should fail on publisherConfirms=false setting`() {
        val rabbitTemplate = getMockedRabbitTemplate(publisherConfirms = false)

        val ex = assertThrows<RuntimeException> {
            RabbitMessagePublisher(rabbitTemplate, "x")
        }
        Assertions.assertThat(ex.message).contains("publisher-confirm-type")
    }

    @Test
    fun `initialization should fail on publisherReturns=false setting`() {
        val rabbitTemplate = getMockedRabbitTemplate(publisherReturns = false)

        val ex = assertThrows<RuntimeException> {
            RabbitMessagePublisher(rabbitTemplate, "x")
        }
        Assertions.assertThat(ex.message).contains("publisher-returns")
    }

    @Test
    fun `initialization should fail on mandatory=false setting`() {
        val rabbitTemplate = getMockedRabbitTemplate(mandatoryMessage = false)

        val ex = assertThrows<RuntimeException> {
            RabbitMessagePublisher(rabbitTemplate, "x")
        }
        Assertions.assertThat(ex.message).contains("mandatory")
    }

    private fun getMockedRabbitTemplate(
        publisherConfirms: Boolean = true,
        publisherReturns: Boolean = true,
        mandatoryMessage: Boolean = true
    ): RabbitTemplate {
        val connectionFactory: ConnectionFactory = mock()
        val rabbitTemplate: RabbitTemplate = mock()
        whenever(rabbitTemplate.connectionFactory).thenReturn(connectionFactory)
        whenever(rabbitTemplate.connectionFactory.isPublisherConfirms).thenReturn(publisherConfirms)
        whenever(rabbitTemplate.connectionFactory.isPublisherReturns).thenReturn(publisherReturns)
        whenever(rabbitTemplate.isMandatoryFor(any())).thenReturn(mandatoryMessage)

        return rabbitTemplate
    }
}