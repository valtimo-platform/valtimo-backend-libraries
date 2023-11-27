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

package com.ritense.outbox.service

import com.ritense.outbox.domain.OutboxMessage
import com.ritense.outbox.repository.OutboxMessageRepository
import mu.KotlinLogging
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

open class DefaultOutboxService(
    private val outboxMessageRepository: OutboxMessageRepository
) : OutboxService {

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
    override fun send(aggregateRootId: String, eventType: String, message: String) {
        outboxMessageRepository.save(
            OutboxMessage(
                id = UUID.randomUUID(),
                message = message,
                createdOn = LocalDateTime.now(),
                aggregateRootId = aggregateRootId,
                eventType = eventType
            )
        )
    }

}