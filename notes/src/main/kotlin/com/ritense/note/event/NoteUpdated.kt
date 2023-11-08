package com.ritense.note.event

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.outbox.domain.BaseEvent

class NoteUpdated (noteId: String, note: ObjectNode) : BaseEvent(
    type = "com.ritense.valtimo.note.updated",
    resultType = "com.ritense.document.domain.Note",
    resultId = noteId,
    result = note
)