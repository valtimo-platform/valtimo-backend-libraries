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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

open class OutboxService(
    private val outboxMessageRepository: OutboxMessageRepository,
    private val messagePublisher: MessagePublisher,
    private val objectMapper: ObjectMapper
) {

    @Transactional(propagation = Propagation.MANDATORY)
    open fun send(message: Any) {
        send(objectMapper.valueToTree(message), message::class.simpleName!!)
    }

    /**
     * Typical workflow
     * @Transactional
     * service.doSomething(
     *      orderRepo.save(order)
     *      order.events.forEach { outboxService.send(it) }
     *      order.events.clear()
     * )
     */
    @Transactional(propagation = Propagation.MANDATORY)
    open fun send(message: ObjectNode, eventType: String = message::class.simpleName!!) {
        val outboxMessage = OutboxMessage(
            message = message,
            eventType = eventType
        )
        logger.debug { "Saving OutboxMessage '${outboxMessage.id}'" }
        outboxMessageRepository.save(outboxMessage)

        // Fire and forget. All events are published in order. If it fails, the events still exists inside the outbox table.
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            try {
                publishAll()
            } catch (e: Exception) {
                logger.debug { e } //
            }
        }
    }

    open fun publishAll() {
        outboxMessageRepository.findByOrderByCreatedOnAsc().forEach { message ->
            messagePublisher.publish(message)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

