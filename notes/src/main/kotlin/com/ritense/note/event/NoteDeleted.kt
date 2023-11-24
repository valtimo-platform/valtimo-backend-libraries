package com.ritense.note.event

import com.ritense.outbox.domain.BaseEvent

class NoteDeleted(noteId: String) : BaseEvent(
    type = "com.ritense.valtimo.note.deleted",
    resultType = null,
    resultId = noteId,
    result = null
)