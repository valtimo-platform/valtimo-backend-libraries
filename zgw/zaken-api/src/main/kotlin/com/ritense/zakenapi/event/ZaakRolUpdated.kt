package com.ritense.zakenapi.event

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.outbox.domain.BaseEvent

class ZaakRolUpdated (zaakRolUrl: String, rol: ObjectNode) : BaseEvent(
    type = "com.ritense.gzac.zrc.rol.updated",
    resultType = "com.ritense.zakenapi.domain.rol.Rol",
    resultId = zaakRolUrl,
    result = rol
)