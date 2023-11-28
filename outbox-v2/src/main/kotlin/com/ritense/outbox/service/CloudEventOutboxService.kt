package com.ritense.outbox.service

import io.cloudevents.CloudEvent
import io.cloudevents.core.provider.EventFormatProvider
import io.cloudevents.jackson.JsonFormat

class CloudEventOutboxService(
    private val outboxService: OutboxService
) {
    fun send(aggregateRootId: String, message: CloudEvent) {
        outboxService.send(
            aggregateRootId,
            message.type,
            EventFormatProvider.getInstance().resolveFormat(JsonFormat.CONTENT_TYPE)!!.serialize(message)
                .toString(Charsets.UTF_8)
        )
    }
}