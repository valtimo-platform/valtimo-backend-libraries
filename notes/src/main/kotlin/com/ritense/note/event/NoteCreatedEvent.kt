package com.ritense.note.event

import com.ritense.valtimo.contract.audit.AuditEvent
import com.ritense.valtimo.contract.audit.utils.AuditHelper
import com.ritense.valtimo.contract.utils.RequestHelper
import java.time.LocalDateTime
import java.util.UUID

data class NoteCreatedEvent(
    private val id: UUID,
    private val origin: String,
    private val occurredOn: LocalDateTime,
    private val user: String,
    private val documentId: UUID?,
    val noteId: UUID,
) : AuditEvent {

    constructor(documentId: UUID, noteId: UUID) : this(
        UUID.randomUUID(),
        RequestHelper.getOrigin(),
        LocalDateTime.now(),
        AuditHelper.getActor(),
        documentId,
        noteId,
    )

    override fun getId(): UUID {
        return id
    }

    override fun getOrigin(): String {
        return origin
    }

    override fun getOccurredOn(): LocalDateTime {
        return occurredOn
    }

    override fun getUser(): String {
        return user
    }

    override fun getDocumentId(): UUID? {
        return documentId
    }
}
