package com.ritense.objectenapi.event

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.outbox.domain.BaseEvent

class ObjectUpdated(objectUrl: String, updatedObject: ObjectNode) : BaseEvent(
    type = "com.ritense.gzac.objecten-api.object.updated",
    resultType = "com.ritense.objectenapi.client.ObjectWrapper",
    resultId = objectUrl,
    result = updatedObject
)