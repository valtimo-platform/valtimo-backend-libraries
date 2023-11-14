package com.ritense.zakenapi.event

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.outbox.domain.BaseEvent
import java.util.UUID

class DocumentLinkedToZaak (documentUuid: UUID, result: ObjectNode) : BaseEvent(
    type = "com.ritense.gzac.zrc.zaakinformatieobject.linked",
    resultType = "com.ritense.zakenapi.client.LinkDocumentResult",
    resultId = documentUuid.toString(),
    result = result
)