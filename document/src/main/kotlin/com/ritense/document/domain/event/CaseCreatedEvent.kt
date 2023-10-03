package com.ritense.document.domain.event

import com.fasterxml.jackson.annotation.JsonTypeName
import com.ritense.valtimo.web.sse.event.BaseSseEvent
import java.util.UUID

class CaseCreatedEvent(
    val documentId: UUID
) : BaseSseEvent("CASE_CREATED") {

}