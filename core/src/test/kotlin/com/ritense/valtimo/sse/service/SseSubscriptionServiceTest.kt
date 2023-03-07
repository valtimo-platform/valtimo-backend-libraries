package com.ritense.valtimo.sse.service

import com.ritense.valtimo.sse.event.TaskUpdateSseEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.UUID

class SseSubscriptionServiceTest {

    private val sseSubscriptionService: SseSubscriptionService = SseSubscriptionService()

    @Test
    fun `subscribe without subscriptionId`() {
        val subscriber = sseSubscriptionService.subscribe()
        assertThat(subscriber).isNotNull
    }

    @Test
    fun `subscribe with existing subscriptionId`() {
        val subscriptionId = UUID.randomUUID()
        val subscriber = sseSubscriptionService.subscribe(subscriptionId)
        assertThat(subscriber).isNotNull
    }

    @Test
    fun notifySubscribers() {
        val subscriptionId = UUID.randomUUID()
        sseSubscriptionService.subscribe(subscriptionId)
        assertDoesNotThrow {
            sseSubscriptionService.notifySubscribers(
                TaskUpdateSseEvent(
                    processInstanceId = "processInstanceId"
                )
            )
        }
    }

    @Test
    fun remove() {
        val subscriptionId = UUID.randomUUID()
        sseSubscriptionService.subscribe(subscriptionId)
        assertDoesNotThrow {
            sseSubscriptionService.remove(subscriptionId)
        }
    }

}