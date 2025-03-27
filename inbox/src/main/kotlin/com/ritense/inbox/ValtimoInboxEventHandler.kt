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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.cloudevents.core.provider.EventFormatProvider
import io.cloudevents.jackson.JsonFormat

class ValtimoInboxEventHandler(
    private val eventHandlers: List<ValtimoEventHandler>,
    private val objectMapper: ObjectMapper
): InboxEventHandler {
    val cloudEventFormat = EventFormatProvider
        .getInstance()
        .resolveFormat(JsonFormat.CONTENT_TYPE)!!

    override fun handle(event: Any) {
        event as String
        val deserializedEvent = try {
            val cloudEvent = cloudEventFormat.deserialize(event.encodeToByteArray())
            val cloudEventData = cloudEvent.data?.let { objectMapper.readValue<CloudEventData>(it.toBytes()) }

            ValtimoEvent(
                id = cloudEvent.id,
                type = cloudEvent.type,
                date = cloudEvent.time?.toLocalDateTime(),
                userId = cloudEventData?.userId,
                roles = cloudEventData?.roles,
                resultType = cloudEventData?.resultType,
                resultId = cloudEventData?.resultId,
                result = cloudEventData?.result
            )
        } catch (e: Exception) {
            //ignore messages that can't be parsed as cloud events
            null
        }

        deserializedEvent?.let {
            eventHandlers.forEach { handler -> handler.handle(it) }
        }
    }
}