package com.ritense.idempotency.service

import com.ritense.idempotency.domain.IdempotencyEvent
import com.ritense.idempotency.repository.IdempotencyEventRepository
import org.springframework.transaction.annotation.Transactional

open class IdempotencyEventService(
    private val idempotencyEventRepository: IdempotencyEventRepository
) {
    @Transactional
    open fun isProcessed(consumer: String, messageId: String) =
        idempotencyEventRepository.existsByConsumerAndMessageId(consumer, messageId)

    @Transactional
    open fun store(idempotencyEvent: IdempotencyEvent): IdempotencyEvent =
        idempotencyEventRepository.saveAndFlush(idempotencyEvent)
}