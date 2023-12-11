package com.ritense.documentenapi.event

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.outbox.domain.BaseEvent

class DocumentInformatieObjectViewed (documentInformatieobjectUrl: String, documentInformatieObject: ObjectNode) : BaseEvent(
    type = "com.ritense.gzac.drc.enkelvoudiginformatieobject.viewed",
    resultType = "com.ritense.documentenapi.client.DocumentInformatieObject",
    resultId = documentInformatieobjectUrl,
    result = documentInformatieObject
)