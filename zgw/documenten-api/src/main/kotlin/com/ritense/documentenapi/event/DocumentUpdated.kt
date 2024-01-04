package com.ritense.documentenapi.event

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.outbox.domain.BaseEvent

class DocumentUpdated (documentUrl: String, document: ObjectNode) : BaseEvent(
    type = "com.ritense.gzac.drc.document.updated",
    resultType = "com.ritense.documentenapi.client.DocumentInformatieObject",
    resultId = documentUrl,
    result = document
)