package com.ritense.idempotency.repository

import com.ritense.idempotency.domain.IdempotencyEvent
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface IdempotencyEventRepository : JpaRepository<IdempotencyEvent, UUID> {
    fun existsByConsumerAndEventId(consumer: String, eventId: String): Boolean
}