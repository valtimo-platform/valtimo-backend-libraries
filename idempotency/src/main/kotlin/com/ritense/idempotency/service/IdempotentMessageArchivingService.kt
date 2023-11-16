package com.ritense.idempotency.service

import com.ritense.idempotency.repository.IdempotentMessageRepository
import java.time.LocalDateTime
import org.springframework.scheduling.annotation.Scheduled

class IdempotentMessageDeletionService(
    private val IdempotentMessageRepository: IdempotentMessageRepository
) {

    @Scheduled(cron = "\${cron-deletion-timer}")
    fun deleteAll() =
        IdempotentMessageRepository.deleteAllOlderThan(LocalDateTime.now().minusMonths("\${archive-message-after}".toLong()))
}