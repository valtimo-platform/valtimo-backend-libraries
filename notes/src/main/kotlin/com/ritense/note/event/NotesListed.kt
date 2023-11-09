package com.ritense.note.event

import com.fasterxml.jackson.databind.node.ArrayNode
import com.ritense.outbox.domain.BaseEvent

class NotesListed (notes: ArrayNode) : BaseEvent(
    type = "com.ritense.valtimo.note.listed",
    resultType = "List<com.ritense.document.domain.Note>",
    resultId = null,
    result = notes
)