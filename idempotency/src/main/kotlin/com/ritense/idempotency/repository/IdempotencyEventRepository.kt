package com.ritense.idempotency.repository

import com.ritense.idempotency.domain.IdempotencyEvent
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface IdempotencyEventRepository : JpaRepository<IdempotencyEvent, UUID> {
    fun existsByConsumerAndMessageId(consumer: String, messageId: String): Boolean
}