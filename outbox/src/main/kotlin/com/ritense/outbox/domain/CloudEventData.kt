package com.ritense.outbox.domain
import com.fasterxml.jackson.databind.node.ObjectNode

data class CloudEventData(
    val userId: String,
    val roles: String,
    val resultType: String?,
    val resultId: String?,
    val result: ObjectNode
)