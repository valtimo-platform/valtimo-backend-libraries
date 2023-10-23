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

package com.ritense.outbox.publisher

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.outbox.BaseIntegrationTest
import com.ritense.outbox.OutboxMessage
import com.ritense.outbox.OutboxMessageRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

class PollingPublisherServiceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var pollingPublisherService: PollingPublisherService

    @Autowired
    lateinit var outboxMessageRepository: OutboxMessageRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    @Transactional
    fun `should publish messages`() {
        val event = PollingPublisherJobIntTest.OrderCreatedEvent("textBook")
        val message = OutboxMessage(
            message = objectMapper.valueToTree(event),
            eventType = event::class.simpleName!!
        )
        outboxMessageRepository.save(message)

        pollingPublisherService.pollAndPublishAll()

        assertThat(messagePublisher.publish(message))
    }
}
