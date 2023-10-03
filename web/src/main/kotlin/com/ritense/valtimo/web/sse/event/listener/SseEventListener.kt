package com.ritense.valtimo.web.sse.event.listener

import com.ritense.valtimo.contract.audit.AuditEvent
import com.ritense.valtimo.web.sse.event.BaseSseEvent
import com.ritense.valtimo.web.sse.service.SseSubscriptionService
import org.springframework.context.event.EventListener

class SseEventListener(
    private val sseSubscriptionService: SseSubscriptionService
) {
    @EventListener(classes = [BaseSseEvent::class])
    fun handle(event: BaseSseEvent) {
        sseSubscriptionService.notifySubscribers(event)
    }
}