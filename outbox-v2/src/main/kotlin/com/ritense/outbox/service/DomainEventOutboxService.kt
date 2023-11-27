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
        cloudEventOutboxService.send(cloudEvent, message.aggregateId(), message.getFilterKey(), message.getFilterKey())
    }

    override fun getOldestMessage() = cloudEventOutboxService.getOldestMessage()

    override fun deleteMessage(id: UUID) = cloudEventOutboxService.deleteMessage(id)
}