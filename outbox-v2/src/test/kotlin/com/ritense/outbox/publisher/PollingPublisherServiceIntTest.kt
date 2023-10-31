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