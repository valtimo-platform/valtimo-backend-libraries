package com.ritense.documentenapi.event

import com.fasterxml.jackson.databind.node.ArrayNode
import com.ritense.outbox.domain.BaseEvent

class DocumentListed (informatieObjecten: ArrayNode) : BaseEvent(
    type = "com.ritense.gzac.drc.document.listed",
    resultType = "List<com.ritense.documentenapi.client.DocumentInformatieObject>",
    resultId = null,
    result = informatieObjecten
)