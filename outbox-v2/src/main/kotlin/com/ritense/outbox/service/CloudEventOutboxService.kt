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