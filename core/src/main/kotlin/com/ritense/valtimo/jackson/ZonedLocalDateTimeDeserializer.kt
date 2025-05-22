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

package com.ritense.valtimo.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import mu.KLogger
import mu.KotlinLogging
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * The LocalDateTimeDeserializer provided by the JavaTimeModule does not support ISO-8601 values with an added time offset (for example: +2:00).
 * Next to that, the LocalDateTimeDeserializer accepts a 'Z' offset (UTC), but does not compensate the result with the local offset.
 *
 * This deserializer tries to parse a String value as a ZonedDateTime first.
 * If it can parse the value as a ZonedDateTime, the value converted into a LocalDateTime with the zone offsets taken into account.
 * If the value cannot be parsed into a ZonedDateTime, it is parsed as a LocalDateTime and returned.
 *
 * If the above fails, it falls back to the LocalDateTimeDeserializer default behaviour.
 */
class ZonedLocalDateTimeDeserializer : LocalDateTimeDeserializer() {
    override fun _fromString(p: JsonParser, ctxt: DeserializationContext?, value: String): LocalDateTime? {
        val stringValue = value.trim()
        if (stringValue.isEmpty()) {
            return _fromEmptyString(p, ctxt, stringValue)
        }
        return try {
            val result = DateTimeFormatter.ISO_DATE_TIME.parseBest(
                stringValue,
                ZonedDateTime::from,
                LocalDateTime::from
            )

            when (result) {
                is ZonedDateTime -> result.withZoneSameInstant(OffsetDateTime.now().offset).toLocalDateTime()
                is LocalDateTime -> result
                else -> throw DateTimeException("DateTime could not be parsed as LocalDateTime or ZonedDateTime")
            }
        } catch (e: DateTimeException) {
            logger.debug (e) { "Could not parse as ISO_DATE_TIME value. Trying default deserializer." }
            super._fromString(p, ctxt, value)
        }
    }

    private companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}