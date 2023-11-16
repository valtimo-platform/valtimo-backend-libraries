package com.ritense.idempotency.repository

import com.ritense.idempotency.domain.IdempotentMessage
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface IdempotentMessageRepository : JpaRepository<IdempotentMessage, UUID> {
    fun existsByConsumerAndMessageId(consumer: String, messageId: String): Boolean

    @Transactional
    @Modifying
    @Query("DELETE FROM IdempotentMessage e WHERE e.processedOn < :cutoffDate")
    fun deleteAllOlderThan(@Param("cutoffDate") cutoffDate: LocalDateTime)
}