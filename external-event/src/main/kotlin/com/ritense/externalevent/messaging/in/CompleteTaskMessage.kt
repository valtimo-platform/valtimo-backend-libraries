package com.ritense.externalevent.messaging.`in`

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.externalevent.messaging.ExternalDomainMessage

data class CompleteTaskMessage(
    val taskId: String,
    val externalCaseId: String,
    val submission: ObjectNode
) : ExternalDomainMessage()