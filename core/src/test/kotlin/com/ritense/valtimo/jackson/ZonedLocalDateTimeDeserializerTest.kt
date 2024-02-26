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

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId

class ZonedLocalDateTimeDeserializerTest {


    @Test
    fun `should deserialize datetime without zone`() {
        val dateTimeString = """"$DATETIME_STRING""""
        val result: LocalDateTime = mapper.readValue(dateTimeString)

        assertThat(result).isEqualTo(LOCAL_DATE_TIME)
    }

    @Test
    fun `should deserialize datetime zone`() {
        val offsetHours = 5
        val dateTimeString = """"$DATETIME_STRING+0$offsetHours:00""""
        val result: LocalDateTime = mapper.readValue(dateTimeString)
        val offset = ZoneId.systemDefault().rules.getOffset(LocalDateTime.now())
        assertThat(result).isEqualTo(LOCAL_DATE_TIME.minusSeconds((offsetHours * 3600 - offset.totalSeconds).toLong()))
    }

    @Test
    fun `should deserialize datetime Z zone`() {
        val dateTimeString = """"${DATETIME_STRING}Z""""
        val result: LocalDateTime = mapper.readValue(dateTimeString)
        val offset = ZoneId.systemDefault().rules.getOffset(LocalDateTime.now())
        assertThat(result).isEqualTo(LOCAL_DATE_TIME.plusSeconds(offset.totalSeconds.toLong()))
    }

    @Test
    fun `should deserialize to null`() {
        val dateTimeString = """"""""
        val result: LocalDateTime = mapper.readValue(dateTimeString)
        assertThat(result).isNull()
    }

    companion object {
        private const val DATETIME_STRING = "2024-02-15T14:39:54.746"
        private val LOCAL_DATE_TIME = LocalDateTime.of(2024, 2, 15, 14, 39, 54, 746 * 1000000)
        private val mapper = jacksonMapperBuilder()
            .addModule(JavaTimeModule().addDeserializer(LocalDateTime::class.java, ZonedLocalDateTimeDeserializer()))
            .build()
    }
}