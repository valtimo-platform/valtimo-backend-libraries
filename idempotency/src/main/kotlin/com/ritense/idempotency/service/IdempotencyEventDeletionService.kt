package com.ritense.idempotency.service

import com.ritense.idempotency.repository.IdempotencyEventRepository
import org.springframework.scheduling.annotation.Scheduled

open class IdempotencyEventDeletionService(
    private val idempotencyEventRepository: IdempotencyEventRepository
) {

    @Scheduled(cron = "\${cron-deletion-timer}")
    fun deleteAll() =
        idempotencyEventRepository.deleteAll()
}