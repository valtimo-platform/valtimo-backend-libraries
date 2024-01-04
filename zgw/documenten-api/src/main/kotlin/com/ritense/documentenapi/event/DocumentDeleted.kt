package com.ritense.documentenapi.event

import com.ritense.outbox.domain.BaseEvent

class DocumentDeleted (documentUrl: String) : BaseEvent(
    type = "com.ritense.gzac.drc.document.deleted",
    resultType = "com.ritense.documentenapi.client.DocumentInformatieObject",
    resultId = documentUrl,
    result = null
)