package com.ritense.zakenapi.event

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.outbox.domain.BaseEvent

class ZaakOpschortingSet (zaakUrl: String, zaakOpschorting: ObjectNode) : BaseEvent(
    type = "com.ritense.gzac.zrc.zaak.opschorting.set",
    resultType = "com.ritense.zakenapi.domain.ZaakopschortingResponse",
    resultId = zaakUrl,
    result = zaakOpschorting
)