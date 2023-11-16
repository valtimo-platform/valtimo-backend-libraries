package com.ritense.idempotency.service

import com.ritense.idempotency.repository.IdempotencyMessageRepository
import java.time.LocalDateTime
import org.springframework.scheduling.annotation.Scheduled

class IdempotencyMessageDeletionService(
    private val idempotencyMessageRepository: IdempotencyMessageRepository
) {

    @Scheduled(cron = "\${cron-deletion-timer}")
    fun deleteAll() =
        idempotencyMessageRepository.deleteAllOlderThan(LocalDateTime.now().minusMonths(3))
}