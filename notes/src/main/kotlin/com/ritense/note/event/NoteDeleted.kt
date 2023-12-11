package com.ritense.note.event

import com.ritense.outbox.domain.BaseEvent

class NoteDeleted(noteId: String) : BaseEvent(
    type = "com.ritense.valtimo.note.deleted",
    resultType = "com.ritense.document.domain.Note",
    resultId = noteId,
    result = null
)