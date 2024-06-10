/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.outbox.domain.BaseEvent
import com.ritense.outbox.domain.CloudEventData
import com.ritense.outbox.exception.OutboxTransactionReadOnlyException
import com.ritense.outbox.repository.OutboxMessageRepository
import io.cloudevents.core.builder.CloudEventBuilder
import io.cloudevents.core.provider.EventFormatProvider
import io.cloudevents.jackson.JsonFormat
import mu.KotlinLogging
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.net.URI
import java.time.ZonedDateTime
import java.util.UUID
import java.util.function.Supplier
import kotlin.text.Charsets.UTF_8

open class ValtimoOutboxService(
    private val outboxMessageRepository: OutboxMessageRepository,
    private val objectMapper: ObjectMapper,
    private val userProvider: UserProvider,
    private val cloudEventSource: String,
) : OutboxService {

    @Transactional(propagation = Propagation.MANDATORY)
    override fun send(eventSupplier: Supplier<BaseEvent>) {
        val baseEvent = eventSupplier.get()

        val userId = baseEvent.userId ?: userProvider.getCurrentUserLogin() ?: "System"
        val roles = baseEvent.roles.ifEmpty { userProvider.getCurrentUserRoles() }
        val cloudEventData =
            CloudEventData(userId, roles.toSet(), baseEvent.resultType, baseEvent.resultId, baseEvent.result)
        val cloudEvent = CloudEventBuilder.v1()
            .withId(baseEvent.id.toString())
            .withSource(URI(cloudEventSource))
            .withTime(baseEvent.date.atOffset(ZonedDateTime.now().offset))
            .withType(baseEvent.type)
            .withDataContentType("application/json")
            .withData(objectMapper.writeValueAsBytes(cloudEventData))
            .build()
        val serializedCloudEvent = EventFormatProvider
            .getInstance()
            .resolveFormat(JsonFormat.CONTENT_TYPE)!!
            .serialize(cloudEvent)
        val serializedCloudEventString = String(serializedCloudEvent, UTF_8)

        send(serializedCloudEventString)
    }

    /**
     * Guarantee that the message is published using the transactional outbox pattern.
     * See: https://microservices.io/patterns/data/transactional-outbox.html
     *
     * Typical workflow:
     * @Transactional
     * fun saveOrders() {
     *      orderRepo.save(order)
     *      order.events.forEach { outboxService.send(it) }
     *      order.events.clear()
     * }
     */
    @Transactional(propagation = Propagation.MANDATORY)
    open fun send(message: String) {
        if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            throw OutboxTransactionReadOnlyException()
        }

        val outboxMessage = OutboxMessage(
            message = message
        )
        logger.debug { "Saving OutboxMessage '${outboxMessage.id}'" }
        outboxMessageRepository.save(outboxMessage)
    }

    open fun getOldestMessage() = outboxMessageRepository.findOutboxMessage()

    open fun deleteMessage(id: UUID) = outboxMessageRepository.deleteById(id)

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
