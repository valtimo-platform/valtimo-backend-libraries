package com.ritense.idempotency.service

import com.ritense.idempotency.domain.IdempotencyEvent
import com.ritense.idempotency.repository.IdempotencyEventRepository

class IdempotencyEventService(
    private val idempotencyEventRepository: IdempotencyEventRepository,
) {
    fun check(consumer: String, eventId: String) =
        idempotencyEventRepository.existsByConsumerAndEventId(consumer, eventId)

    fun store(idempotencyEvent: IdempotencyEvent) =
        idempotencyEventRepository.save(idempotencyEvent)
}