package com.ritense.zakenapi.event

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.outbox.domain.BaseEvent

class ZaakCreated (zaakUrl: String, zaak: ObjectNode) : BaseEvent(
    type = "com.ritense.gzac.zrc.zaak.created",
    resultType = "com.ritense.zakenapi.domain.CreateZaakResponse",
    resultId = zaakUrl,
    result = zaak
)