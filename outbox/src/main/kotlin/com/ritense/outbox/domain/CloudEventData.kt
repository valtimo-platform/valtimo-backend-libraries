package com.ritense.outbox.domain

import com.fasterxml.jackson.databind.node.ContainerNode

data class CloudEventData(
    val userId: String,
    val roles: Set<String>,
    val resultType: String?,
    val resultId: String?,
    val result: ContainerNode<*>?
)