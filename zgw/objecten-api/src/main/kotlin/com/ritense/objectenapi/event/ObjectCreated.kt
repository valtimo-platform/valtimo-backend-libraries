package com.ritense.objectenapi.event

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.outbox.domain.BaseEvent

class ObjectCreated(objectUrl: String, createdObject: ObjectNode) : BaseEvent(
    type = "com.ritense.gzac.objecten-api.object.created",
    resultType = "com.ritense.objecten-api.client.ObjectWrapper",
    resultId = objectUrl,
    result = createdObject
)