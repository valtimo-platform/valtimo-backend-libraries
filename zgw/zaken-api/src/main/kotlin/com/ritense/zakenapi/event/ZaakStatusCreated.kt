package com.ritense.zakenapi.event

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.outbox.domain.BaseEvent

class ZaakStatusCreated  (zaakStatusUrl: String, zaakStatus: ObjectNode) : BaseEvent(
    type = "com.ritense.gzac.zrc.zaakstatus.created",
    resultType = "com.ritense.zakenapi.domain.CreateZaakStatusResponse",
    resultId = zaakStatusUrl,
    result = zaakStatus
)