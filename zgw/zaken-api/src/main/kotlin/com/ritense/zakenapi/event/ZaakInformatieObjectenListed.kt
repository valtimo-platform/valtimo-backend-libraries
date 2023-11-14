package com.ritense.zakenapi.event

import com.fasterxml.jackson.databind.node.ArrayNode
import com.ritense.outbox.domain.BaseEvent

class ZaakInformatieObjectenListed (zaakInformatieObjecten: ArrayNode) : BaseEvent(
    type = "com.ritense.gzac.zrc.zaakinformatieobject.listed",
    resultType = "List<com.ritense.zakenapi.domain.ZaakInformatieObject>",
    resultId = null,
    result = zaakInformatieObjecten
)