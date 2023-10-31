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
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.transaction.annotation.Transactional

@Transactional
class PollingPublisherServiceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var pollingPublisherService: PollingPublisherService

    @SpyBean
    lateinit var messagePublisher: DefaultMessagePublisher

    /* @Value("\${polling-publisher.retry.max-attempts}")
     lateinit var maxRetryExpression: String*/

    @Test
    fun `should send OutboxMessage`() {
        val event = OrderCreatedEvent("textBook")

        assertThat(outboxService.getOldestMessage()).isNull()

        outboxService.send(event)
        assertThat(outboxService.getOldestMessage()).isNotNull()

        pollingPublisherService.pollAndPublishAll()

        assertThat(outboxService.getOldestMessage()).isNull()
        verify(messagePublisher).publish(any())
    }

    /* @Test
     fun `should send multiple OutboxMessages`() {
         val event = OrderCreatedEvent("textBook")

         assertThat(outboxService.getMessages().size).isEqualTo(0)

         outboxService.send(event)
         outboxService.send(event)
         outboxService.send(event)
         assertThat(outboxService.getMessages().size).isEqualTo(3)

         pollingPublisherService.pollMessage()

         assertThat(outboxService.getMessages().size).isEqualTo(0)
         verify(messagePublisher, times(3)).publish(any())
     }

     @Test
     fun `should not delete OutboxMessage when sending fails`() {
         val event = OrderCreatedEvent("textBook")

         assertThat(outboxService.getMessages().size).isEqualTo(0)

         outboxService.send(event)
         assertThat(outboxService.getMessages().size).isEqualTo(1)

         try {
             doThrow(RuntimeException("Haha")).whenever(messagePublisher).publish(any())
             pollingPublisherService.pollMessage()
             Assertions.fail("Error message not thrown")
         } catch (e: Exception) {
             assertThat(outboxService.getMessages().size).isEqualTo(1)
             verify(messagePublisher, times(maxRetryExpression.toInt())).publish(any())
         }
     }*/

    data class OrderCreatedEvent(
        val name: String
    )
}