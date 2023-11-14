package com.ritense.zakenapi.event

import com.fasterxml.jackson.databind.node.ArrayNode
import com.ritense.outbox.domain.BaseEvent

class ZaakRollenListed (zaakRollen: ArrayNode) : BaseEvent(
    type = "com.ritense.gzac.zrc.rol.listed",
    resultType = "List<com.ritense.zakenapi.domain.rol.Rol>",
    resultId = null,
    result = zaakRollen
)