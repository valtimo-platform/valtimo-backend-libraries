package com.ritense.idempotent.service

import com.ritense.idempotent.domain.IdempotentMessage
import com.ritense.idempotent.repository.IdempotentMessageRepository
import org.springframework.transaction.annotation.Transactional

@Transactional
class IdempotentMessageService(
    private val idempotentMessageRepository: IdempotentMessageRepository
) {
    fun isProcessed(consumer: String, messageId: String) =
        idempotentMessageRepository.existsByConsumerAndMessageId(consumer, messageId)

    fun store(idempotentMessage: IdempotentMessage): IdempotentMessage =
        idempotentMessageRepository.saveAndFlush(idempotentMessage)
}