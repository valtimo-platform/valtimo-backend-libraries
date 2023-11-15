package com.ritense.zakenapi.event

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.outbox.domain.BaseEvent

class ZaakViewed (zaakUrl: String, zaak: ObjectNode) : BaseEvent(
    type = "com.ritense.gzac.zrc.zaak.viewed",
    resultType = "com.ritense.zakenapi.domain.ZaakResponse",
    resultId = zaakUrl,
    result = zaak
)