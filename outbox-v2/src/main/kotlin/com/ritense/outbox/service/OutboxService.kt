package com.ritense.outbox.service

import com.ritense.outbox.domain.OutboxMessage
import java.util.UUID

interface OutboxService<T> {

    fun send(message: T)

    fun getOldestMessage(): OutboxMessage?

    fun deleteMessage(id: UUID)
}