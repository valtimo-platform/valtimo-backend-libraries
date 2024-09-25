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

package com.ritense.logging.service

import com.ritense.logging.BaseIntegrationTest
import com.ritense.logging.domain.LoggingEvent
import com.ritense.logging.domain.LoggingEventProperty
import com.ritense.logging.domain.LoggingEventPropertyId
import com.ritense.logging.web.rest.dto.LoggingEventPropertyDto
import com.ritense.logging.web.rest.dto.LoggingEventSearchRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random

@Transactional
class LoggingEventServiceIT : BaseIntegrationTest() {

    @Test
    fun `should search logs paginated`() {
        val now = LocalDateTime.now()
        saveLoggingEvent("event 1", now - Duration.ofDays(1))
        val searchRequest = LoggingEventSearchRequest(beforeTimestamp = now - Duration.ofDays(2))
        saveLoggingEvent("event 2", now - Duration.ofDays(3))
        saveLoggingEvent("event 3", now - Duration.ofDays(4))
        saveLoggingEvent("event 4", now - Duration.ofDays(5))
        val sort = Sort.by(Sort.Direction.DESC, "timestamp")

        val page1 = loggingEventService.searchLoggingEvents(searchRequest, PageRequest.of(0, 2).withSort(sort))
        val page2 = loggingEventService.searchLoggingEvents(searchRequest, PageRequest.of(1, 2).withSort(sort))

        assertEquals(2, page1.count())
        assertEquals("event 2", page1.content[0].formattedMessage)
        assertEquals("event 3", page1.content[1].formattedMessage)
        assertEquals(1, page2.count())
        assertEquals("event 4", page2.content[0].formattedMessage)
    }

    @Test
    fun `should search logs by search queries`() {
        val yesterday = LocalDateTime.now() - Duration.ofDays(1)
        saveLoggingEvent("test event", yesterday - Duration.ofDays(1))
        saveLoggingEvent("another event", yesterday - Duration.ofDays(1))
        val searchRequest = LoggingEventSearchRequest(
            afterTimestamp = yesterday.minusDays(2),
            beforeTimestamp = yesterday,
            level = "INFO",
            likeFormattedMessage = "test",
        )

        val results = loggingEventService.searchLoggingEvents(searchRequest, Pageable.unpaged()).content

        assertEquals(1, results.size)
        assertEquals("test event", results[0].formattedMessage)
    }

    @Test
    fun `should search logs by search properties`() {
        val yesterday = LocalDateTime.now() - Duration.ofDays(1)
        val event1 = saveLoggingEvent("Event 1", yesterday - Duration.ofDays(1))
        val event2 = saveLoggingEvent("Event 2", yesterday - Duration.ofDays(1))
        saveLoggingEventProp(event1, "key A", "value A")
        saveLoggingEventProp(event1, "key B", "value B")
        saveLoggingEventProp(event2, "key A", "value A")
        val searchRequestA = LoggingEventSearchRequest(
            properties = listOf(LoggingEventPropertyDto("key A", "value A"))
        )
        val searchRequestB = LoggingEventSearchRequest(
            properties = listOf(LoggingEventPropertyDto("key B", "value B"))
        )
        val searchRequestC = LoggingEventSearchRequest(
            properties = listOf(LoggingEventPropertyDto("key A", "value B"))
        )

        val resultsA = loggingEventService.searchLoggingEvents(searchRequestA, Pageable.unpaged()).content
        val resultsB = loggingEventService.searchLoggingEvents(searchRequestB, Pageable.unpaged()).content
        val resultsC = loggingEventService.searchLoggingEvents(searchRequestC, Pageable.unpaged()).content

        assertEquals(2, resultsA.size)
        assertTrue(resultsA.map { it.formattedMessage }.contains("Event 1"))
        assertTrue(resultsA.map { it.formattedMessage }.contains("Event 2"))
        assertEquals(1, resultsB.size)
        assertEquals("Event 1", resultsB[0].formattedMessage)
        assertEquals(0, resultsC.size)
    }

    private fun saveLoggingEvent(message: String, localDateTime: LocalDateTime = LocalDateTime.now()): LoggingEvent {
        val timestamp = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return loggingEventRepository.save(
            LoggingEvent(
                id = Random.nextLong(),
                timestamp = timestamp,
                formattedMessage = message,
                loggerName = "loggerName",
                level = "INFO",
                threadName = "threadName",
                referenceFlag = 0,
                arg0 = "arg0",
                arg1 = "arg1",
                arg2 = "arg2",
                arg3 = "arg3",
                callerFilename = "callerFilename",
                callerClass = "callerClass",
                callerMethod = "callerMethod",
                callerLine = 0,
                properties = emptyList(),
                exceptions = emptyList()
            )
        )
    }

    private fun saveLoggingEventProp(event: LoggingEvent, key: String, value: String) {
        loggingEventPropertyRepository.save(
            LoggingEventProperty(
                id = LoggingEventPropertyId(event.id, key),
                event = event,
                value = value
            )
        )
    }
}