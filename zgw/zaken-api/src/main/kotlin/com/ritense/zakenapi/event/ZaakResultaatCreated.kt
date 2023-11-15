package com.ritense.zakenapi.event

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.outbox.domain.BaseEvent

class ZaakResultaatCreated (zaakResultaatUrl: String, zaakResultaat: ObjectNode) : BaseEvent(
    type = "com.ritense.gzac.zrc.zaakresultaat.created",
    resultType = "com.ritense.zakenapi.domain.CreateZaakResultaatResponse",
    resultId = zaakResultaatUrl,
    result = zaakResultaat
)