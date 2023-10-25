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

package com.ritense.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.outbox.domain.BaseEvent
import io.cloudevents.core.builder.CloudEventBuilder
import mu.KotlinLogging
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

open class OutboxService(
    private val outboxMessageRepository: OutboxMessageRepository,
    private val objectMapper: ObjectMapper,
    private val springApplicationName: String?,
    private val valtimoSystemUserId: String?
) {

    @Transactional(propagation = Propagation.MANDATORY)
    open fun send(message: BaseEvent) {
//        message.source = valtimoSystemUserId ?: springApplicationName
        // transform base event to cloud event
        val cloudEvent = CloudEventBuilder.v1().withId(
            message.id.toString()
        ).build()
        send(objectMapper.writeValueAsString(cloudEvent))
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
        val outboxMessage = OutboxMessage(
            message = message
        )
        logger.debug { "Saving OutboxMessage '${outboxMessage.id}'" }
        outboxMessageRepository.save(outboxMessage)
    }

    open fun getOldestMessage() = outboxMessageRepository.findTopByOrderByCreatedOnAsc()

    open fun deleteMessage(id: UUID) = outboxMessageRepository.deleteById(id)

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
