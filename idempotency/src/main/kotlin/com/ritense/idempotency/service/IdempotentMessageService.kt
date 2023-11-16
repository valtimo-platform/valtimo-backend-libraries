package com.ritense.idempotency.service

import com.ritense.idempotency.domain.IdempotentMessage
import com.ritense.idempotency.repository.IdempotentMessageRepository
import org.springframework.transaction.annotation.Transactional

@Transactional
class IdempotentMessageService(
    private val IdempotentMessageRepository: IdempotentMessageRepository
) {
    fun isProcessed(consumer: String, messageId: String) =
        IdempotentMessageRepository.existsByConsumerAndMessageId(consumer, messageId)

    fun store(IdempotentMessage: IdempotentMessage): IdempotentMessage =
        IdempotentMessageRepository.saveAndFlush(IdempotentMessage)
}