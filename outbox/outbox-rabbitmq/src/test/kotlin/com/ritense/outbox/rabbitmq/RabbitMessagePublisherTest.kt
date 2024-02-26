package com.ritense.outbox.rabbitmq

import com.ritense.outbox.OutboxMessage
import com.ritense.outbox.publisher.MessagePublishingFailed
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.core.ReturnedMessage
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate

class RabbitMessagePublisherTest {

    @Test
    fun `initialization should fail on publisherConfirms=false setting`() {
        val rabbitTemplate = getMockedRabbitTemplate(publisherConfirms = false)

        val ex = assertThrows<RuntimeException> {
            RabbitMessagePublisher(rabbitTemplate, "test")
        }
        Assertions.assertThat(ex.message).contains("publisher-confirm-type")
    }

    @Test
    fun `initialization should fail on publisherReturns=false setting`() {
        val rabbitTemplate = getMockedRabbitTemplate(publisherReturns = false)

        val ex = assertThrows<RuntimeException> {
            RabbitMessagePublisher(rabbitTemplate, "test")
        }
        Assertions.assertThat(ex.message).contains("publisher-returns")
    }

    @Test
    fun `initialization should fail on mandatory=false setting`() {
        val rabbitTemplate = getMockedRabbitTemplate(mandatoryMessage = false)

        val ex = assertThrows<RuntimeException> {
            RabbitMessagePublisher(rabbitTemplate, "test")
        }
        Assertions.assertThat(ex.message).contains("mandatory")
    }

    @Test
    fun `should fail when ack is false`() {
        val rabbitTemplate = getMockedRabbitTemplate()

        val publisher = RabbitMessagePublisher(rabbitTemplate, "test")

        val ex = assertThrows<MessagePublishingFailed> {
            whenever(rabbitTemplate.convertAndSend(any<String>(), eq("test"), any<String>(), any<CorrelationData>())).thenAnswer { answer ->
                val correlationData = answer.getArgument(3, CorrelationData::class.java)
                correlationData.future.complete(CorrelationData.Confirm(false, "reasons"))
            }

            publisher.publish(OutboxMessage(message = "test"))

        }

        Assertions.assertThat(ex.message).contains("not acknowledged")
    }

    @Test
    fun `should fail when returned message is not null`() {
        val rabbitTemplate = getMockedRabbitTemplate()

        val publisher = RabbitMessagePublisher(rabbitTemplate, "test")

        val ex = assertThrows<MessagePublishingFailed> {
            whenever(rabbitTemplate.convertAndSend(any<String>(), eq("test"), any<String>(), any<CorrelationData>())).thenAnswer { answer ->
                val message = MessageBuilder.withBody(answer.getArgument(2, String::class.java).toByteArray()).build()
                val correlationData = answer.getArgument(3, CorrelationData::class.java)
                correlationData.returned = ReturnedMessage(
                    message, 0, "returned_message_not_null", "", "")
                correlationData.future.complete(CorrelationData.Confirm(true, "reasons"))
            }

            publisher.publish(OutboxMessage(message = "test"))

        }

        Assertions.assertThat(ex.message).contains("returned_message_not_null")
    }

    @Test
    fun `should fail when message is not confirmed in time`() {
        val rabbitTemplate = getMockedRabbitTemplate()

        val publisher = RabbitMessagePublisher(rabbitTemplate, "test")

        val ex = assertThrows<MessagePublishingFailed> {
            publisher.publish(OutboxMessage(message = "test"))
        }

        Assertions.assertThat(ex.message).contains("not confirmed in time")
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