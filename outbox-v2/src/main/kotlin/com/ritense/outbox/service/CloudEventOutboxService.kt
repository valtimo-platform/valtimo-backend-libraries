package com.ritense.outbox.service

import io.cloudevents.CloudEvent
import io.cloudevents.core.format.EventFormat
import io.cloudevents.core.provider.EventFormatProvider
import io.cloudevents.jackson.JsonFormat
import java.lang.RuntimeException
import java.util.UUID

class CloudEventOutboxService(private val outboxService: OutboxService<Any>): OutboxService<CloudEvent> {

    override fun send(message: CloudEvent) {
        val cloudEventAsJsonString = jsonFormat
            .serialize(message).run {
                String(this, Charsets.UTF_8)
            }
        outboxService.send(cloudEventAsJsonString)
    }

    override fun getOldestMessage() = outboxService.getOldestMessage()

    override fun deleteMessage(id: UUID)  = outboxService.deleteMessage(id)

    companion object {
        val jsonFormat: EventFormat = EventFormatProvider
            .getInstance()
            .resolveFormat(JsonFormat.CONTENT_TYPE) ?: throw RuntimeException("Could not find JSON format")
    }
}