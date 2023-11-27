package com.ritense.outbox.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.core.v1.CloudEventBuilder
import org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE
import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID

class DomainEventOutboxService(
    private val objectMapper: ObjectMapper,
    private val cloudEventOutboxService: CloudEventOutboxService
) {

    fun send(event: DomainEvent) {
        cloudEventOutboxService.send(
            "${event.aggregateType()}:${event.aggregateId()}",
            CloudEventBuilder()
                .withId(UUID.randomUUID().toString())
                .withType(event.eventType())
                .withSource(URI.create("/valtimo"))
                .withTime(OffsetDateTime.now())
                .withDataContentType(APPLICATION_JSON_VALUE)
                .withData(objectMapper.writeValueAsBytes(event))
                .build()
        )
    }

    interface DomainEvent {
        fun eventType(): String
        fun aggregateId(): String
        fun aggregateType(): String
    }
}