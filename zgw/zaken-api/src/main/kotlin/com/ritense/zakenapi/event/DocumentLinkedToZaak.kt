package com.ritense.zakenapi.event

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.outbox.domain.BaseEvent

class DocumentLinkedToZaak (documentUuid: String, result: ObjectNode) : BaseEvent(
    type = "com.ritense.gzac.zrc.zaakinformatieobject.linked",
    resultType = "com.ritense.zakenapi.client.LinkDocumentResult",
    resultId = documentUuid,
    result = result
)