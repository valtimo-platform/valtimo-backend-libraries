/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.inbox

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.cloudevents.core.builder.CloudEventBuilder
import io.cloudevents.core.provider.EventFormatProvider
import io.cloudevents.jackson.JsonFormat
import java.net.URI
import java.util.UUID
import kotlin.text.Charsets.UTF_8

class TestEventHandler(
    val receivedEvents: MutableList<ValtimoEvent> = mutableListOf(),
    var sendEvent: String = "",
) : ValtimoEventHandler {

    override fun handle(event: ValtimoEvent) {
        receivedEvents.add(event)
    }

    fun sendEvent(message: String) {
        val data = CloudEventData(
            userId = "userId",
            roles = emptySet(),
            resultId = null,
            resultType = null,
            result = jacksonObjectMapper().createObjectNode().put("message", message)
        )
        val cloudEvent = CloudEventBuilder.v1()
            .withId(UUID.randomUUID().toString())
            .withType("test")
            .withSource(URI("https://valtimo.com/"))
            .withDataContentType("application/json")
            .withData(jacksonObjectMapper().writeValueAsBytes(data))
            .build()
        val serializedCloudEvent = EventFormatProvider
            .getInstance()
            .resolveFormat(JsonFormat.CONTENT_TYPE)!!
            .serialize(cloudEvent)
        sendEvent = String(serializedCloudEvent, UTF_8)
    }
}