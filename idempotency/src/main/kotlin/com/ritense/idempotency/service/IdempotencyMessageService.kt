package com.ritense.idempotency.service

import com.ritense.idempotency.domain.IdempotencyMessage
import com.ritense.idempotency.repository.IdempotencyMessageRepository
import org.springframework.transaction.annotation.Transactional

@Transactional
class IdempotencyMessageService(
    private val idempotencyMessageRepository: IdempotencyMessageRepository
) {
    fun isProcessed(consumer: String, messageId: String) =
        idempotencyMessageRepository.existsByConsumerAndMessageId(consumer, messageId)

    fun store(idempotencyMessage: IdempotencyMessage): IdempotencyMessage =
        idempotencyMessageRepository.saveAndFlush(idempotencyMessage)
}