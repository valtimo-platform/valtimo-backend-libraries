package com.ritense.objectenapi.event

import com.fasterxml.jackson.databind.node.ArrayNode
import com.ritense.outbox.domain.BaseEvent

class ObjectsListed(objects: ArrayNode) : BaseEvent(
    type = "com.ritense.gzac.objecten-api.object.listed",
    resultType = "List<com.ritense.objecten-api.client.ObjectWrapper>",
    resultId = null,
    result = objects
)