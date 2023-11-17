package com.ritense.idempotent.service

import com.ritense.idempotent.repository.IdempotentMessageRepository
import java.time.LocalDateTime
import org.springframework.scheduling.annotation.Scheduled

class IdempotentMessageArchivingService(
    private val idempotentMessageRepository: IdempotentMessageRepository
) {

    @Scheduled(cron = "\${idempotent.cron-message-archive-deletion-timer}")
    fun deleteAll() =
        idempotentMessageRepository.deleteAllOlderThan(LocalDateTime.now().minusMonths("\${idempotent.archive-message-after-months}".toLong()))
}