/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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