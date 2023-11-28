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

import com.ritense.outbox.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.transaction.annotation.Transactional

@Transactional
class PollingPublisherServiceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var pollingPublisherService: PollingPublisherService

    @Autowired
    lateinit var messagePublisher: DefaultMessagePublisher

    @Test
    fun `should send OutboxMessage`() {
        assertThat(outboxMessageRepository.findTopByOrderByCreatedOnAsc()).isNull()

        defaultOutboxService.send("rootId", "evenType", "MESSAGE")
        assertThat(outboxMessageRepository.findTopByOrderByCreatedOnAsc()).isNotNull()

        pollingPublisherService.pollAndPublishAll()

        assertThat(outboxMessageRepository.findTopByOrderByCreatedOnAsc()).isNull()
    }

    @Test
    fun `should send multiple OutboxMessages`() {
        assertThat(outboxMessageRepository.findAll().size).isEqualTo(0)

        defaultOutboxService.send("rootId", "eventId", "TextBook")
        defaultOutboxService.send("rootId", "eventId", "TextBook")
        defaultOutboxService.send("rootId", "eventId", "TextBook")
        assertThat(outboxMessageRepository.findAll().size).isEqualTo(3)

        pollingPublisherService.pollAndPublishAll()

        assertThat(outboxMessageRepository.findAll().size).isEqualTo(0)
    }

}