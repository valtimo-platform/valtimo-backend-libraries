package com.ritense.documentenapi.event

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.outbox.domain.BaseEvent

class DocumentStored (documentUrl: String, document: ObjectNode) : BaseEvent(
    type = "com.ritense.gzac.drc.document.created",
    resultType = "com.ritense.documentenapi.client.CreateDocumentResult",
    resultId = documentUrl,
    result = document
)