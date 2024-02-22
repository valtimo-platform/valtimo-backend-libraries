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
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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
                is ZonedDateTime -> result.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
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