package com.ritense.objectenapi.event

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.outbox.domain.BaseEvent

class ObjectPatched(objectUrl: String, patchedObject: ObjectNode) : BaseEvent(
    type = "com.ritense.gzac.objecten-api.object.patched",
    resultType = "com.ritense.objectenapi.client.ObjectWrapper",
    resultId = objectUrl,
    result = patchedObject
)