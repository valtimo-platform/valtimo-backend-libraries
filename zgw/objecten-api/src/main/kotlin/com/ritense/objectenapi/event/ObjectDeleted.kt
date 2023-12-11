package com.ritense.objectenapi.event

import com.ritense.outbox.domain.BaseEvent

class ObjectDeleted(objectUrl: String) : BaseEvent(
    type = "com.ritense.gzac.objecten-api.object.deleted",
    resultType = null,
    resultId = objectUrl,
    result = null
)