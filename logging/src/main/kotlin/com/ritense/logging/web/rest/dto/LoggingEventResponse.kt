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

package com.ritense.logging.web.rest.dto

import com.ritense.logging.domain.LoggingEvent
import java.time.LocalDateTime

data class LoggingEventResponse(
    val timestamp: LocalDateTime,
    val formattedMessage: String,
    val level: String,
    val properties: List<LoggingEventPropertyDto>,
    val stacktrace: String?,
) {
    companion object {
        fun of(loggingEvents: List<LoggingEvent>) = loggingEvents.map { of(it) }

        fun of(loggingEvent: LoggingEvent): LoggingEventResponse {
            return LoggingEventResponse(
                timestamp = loggingEvent.getTimestampLocalDateTime(),
                formattedMessage = loggingEvent.formattedMessage,
                level = loggingEvent.level,
                properties = LoggingEventPropertyDto.of(loggingEvent.properties),
                stacktrace = loggingEvent.getStacktrace(),
            )
        }
    }
}