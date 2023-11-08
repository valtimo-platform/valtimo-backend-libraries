package com.ritense.note.event

import com.fasterxml.jackson.databind.node.ArrayNode
import com.ritense.outbox.domain.BaseEvent

class NotesViewed (notes: ArrayNode) : BaseEvent(
    type = "com.ritense.valtimo.note.created",
    resultType = "com.ritense.document.domain.Note",
    resultId = null,
    result = notes
)