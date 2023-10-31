package com.ritense.outbox.service

import com.ritense.outbox.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional

@Transactional
class OutboxServiceIntTest : BaseIntegrationTest() {

    @Test
    fun `should create OutboxMessage`() {
        val event = OrderCreatedEvent("textBook")

        outboxService.send(event)

        val message = outboxService.getOldestMessage()
        assertThat(message?.message).isEqualTo(
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(event)
        )
        assertThat(message?.eventType).isEqualTo("OrderCreatedEvent")
    }

    data class OrderCreatedEvent(
        val name: String
    )
}
