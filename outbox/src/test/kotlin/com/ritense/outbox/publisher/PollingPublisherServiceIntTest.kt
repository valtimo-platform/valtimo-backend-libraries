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

package com.ritense.outbox.publisher

import com.ritense.outbox.BaseIntegrationTest
import com.ritense.outbox.test.OrderCreatedEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired

class PollingPublisherServiceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var pollingPublisherService: PollingPublisherService

    @Test
    fun `should publish messages`() {
        insertOutboxMessage(OrderCreatedEvent("textBook"))

        pollingPublisherService.pollAndPublishAll()

        verify(messagePublisher, times(1)).publish(any())
    }

    @Test
    fun `should not poll messages at the same time because of AtomicBoolean`(): Unit = runBlocking {
        insertOutboxMessage(OrderCreatedEvent("event 1"))
        insertOutboxMessage(OrderCreatedEvent("event 2"))
        whenever(messagePublisher.publish(any())).then { Thread.sleep(1000) }
        verify(outboxMessageRepository, times(0)).findOutboxMessage()

        listOf(
            async(Dispatchers.IO) { pollingPublisherService.pollAndPublishAll() },
            async(Dispatchers.IO) { pollingPublisherService.pollAndPublishAll() }
        ).awaitAll()

        // Number of database reads is 3 because:
        // Poller 1: read database. Find event 1
        // Poller 2: Polling is blocked. NO database read
        // Poller 1: read database. Find event 2
        // Poller 1: read database. Find NULL
        verify(outboxMessageRepository, times(3)).findOutboxMessage()
    }

    @Test
    fun `should poll messages sequentially`() {
        insertOutboxMessage(OrderCreatedEvent("event 1"))
        insertOutboxMessage(OrderCreatedEvent("event 2"))
        whenever(messagePublisher.publish(any())).then { Thread.sleep(1000) }
        verify(outboxMessageRepository, times(0)).findOutboxMessage()

        pollingPublisherService.pollAndPublishAll()
        pollingPublisherService.pollAndPublishAll()

        // Number of database reads is 4 because:
        // Poller 1: read database. Find event 1
        // Poller 1: read database. Find event 2
        // Poller 1: read database. Find NULL
        // Poller 2: read database. Find NULL
        verify(outboxMessageRepository, times(4)).findOutboxMessage()
    }
}
