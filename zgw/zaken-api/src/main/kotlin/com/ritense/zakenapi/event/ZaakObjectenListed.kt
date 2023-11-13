package com.ritense.zakenapi.event

import com.fasterxml.jackson.databind.node.ArrayNode
import com.ritense.outbox.domain.BaseEvent

class ZaakObjectenListed (zaakobjecten: ArrayNode) : BaseEvent(
    type = "com.ritense.gzac.zrc.zaakobject.listed",
    resultType = "List<com.ritense.zakenapi.domain.ZaakObject>",
    resultId = null,
    result = zaakobjecten
)