package com.ritense.outbox.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.outbox.domain.DomainEvent
import io.cloudevents.core.builder.CloudEventBuilder
import org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE
import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID

class DomainEventOutboxService(
    private val cloudEventOutboxService: CloudEventOutboxService,
    private val objectMapper: ObjectMapper
): OutboxService<DomainEvent> {

    override fun send(message: DomainEvent) {
        val cloudEvent = CloudEventBuilder.v1()
            .withId(UUID.randomUUID().toString())
            .withSource(URI("http://allnex"))
            .withTime(OffsetDateTime.now())
            .withType(message::class.java.simpleName)
            .withDataContentType(APPLICATION_JSON_VALUE)
            .withData(objectMapper.writeValueAsBytes(message))
            .build()
        cloudEventOutboxService.send(cloudEvent)
    }

    override fun getOldestMessage() = cloudEventOutboxService.getOldestMessage()

    override fun deleteMessage(id: UUID) = cloudEventOutboxService.deleteMessage(id)
}