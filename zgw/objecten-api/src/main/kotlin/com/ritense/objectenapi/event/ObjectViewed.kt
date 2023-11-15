package com.ritense.objectenapi.event

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.outbox.domain.BaseEvent

class ObjectViewed(objectUrl: String, viewedObject: ObjectNode) : BaseEvent(
    type = "com.ritense.gzac.objecten-api.object.viewed",
    resultType = "com.ritense.objecten-api.client.ObjectWrapper",
    resultId = objectUrl,
    result = viewedObject
)