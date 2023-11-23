package com.ritense.outbox.service

import io.cloudevents.CloudEvent
import io.cloudevents.core.format.EventFormat
import io.cloudevents.core.provider.EventFormatProvider
import io.cloudevents.jackson.JsonFormat

class CloudEventOutboxService(private val outboxService: OutboxService<Any>): OutboxService<CloudEvent> {

    private val jsonFormat: EventFormat = EventFormatProvider
        .getInstance()
        .resolveFormat(JsonFormat.CONTENT_TYPE)!!

    override fun send(message: CloudEvent) {
        val cloudEventAsJsonString = jsonFormat
            .serialize(message).run {
                String(this, Charsets.UTF_8)
            }
        outboxService.send(cloudEventAsJsonString)
    }
}