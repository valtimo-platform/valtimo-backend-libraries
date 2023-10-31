/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.outbox.rabbitmq

import com.ritense.outbox.OutboxMessage
import com.ritense.outbox.publisher.MessagePublisher
import com.ritense.outbox.publisher.MessagePublishingFailed
import java.time.Duration
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import mu.KLogger
import mu.KotlinLogging
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate


class RabbitMessagePublisher(
    private val rabbitTemplate: RabbitTemplate,
    private val queueName: String,
    private val deliveryTimeout: Duration = Duration.ofSeconds(1)
): MessagePublisher {

    init {
        require(rabbitTemplate.connectionFactory.isPublisherConfirms) { "The RabbitMQ outbox publisher requires correlated publisher-confirm-type!" }
        require(rabbitTemplate.connectionFactory.isPublisherReturns) { "The RabbitMQ outbox publisher requires publisher-returns to be enabled!" }
        require(rabbitTemplate.isMandatoryFor(Message("test".toByteArray()))) { "The RabbitMQ outbox publisher requires messages to be mandatory!" }
    }

    override fun publish(message: OutboxMessage) {
        val correlationData = CorrelationData(UUID.randomUUID().toString())
        logger.trace { "Sending message to RabbitMQ: queue=${queueName}, msgId=${message.id}, correlationId= ${correlationData.id}" }

        val queueMessage = MessageBuilder.withBody(message.message.toByteArray()).build()
        rabbitTemplate.convertAndSend(queueName, queueMessage, correlationData)

        try {
            val result = correlationData.future.get(deliveryTimeout.toMillis(), TimeUnit.MILLISECONDS)
            if (!result!!.isAck) {
                throw MessagePublishingFailed("Outbox message was not acknowledged: reason=${result.reason}, queue=${queueName}, msgId=${message.id}, correlationId= ${correlationData.id}\"")
            } else if (correlationData.returned != null) {
                val returned = correlationData.returned
                throw MessagePublishingFailed("Could not deliver outbox message: routingKey=${returned.routingKey}, code=${returned.replyCode}, msg=${returned.replyText}, queue=${queueName}, msgId=${message.id}, correlationId= ${correlationData.id}\"")
            }
        } catch (timeoutException: TimeoutException) {
            throw MessagePublishingFailed("Outbox message delivery was not confirmed in time: queue=${queueName}, msgId=${message.id}, correlationId= ${correlationData.id}")
        }
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}