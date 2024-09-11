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
import com.ritense.logging.web.rest.dto.LoggingEventSearchRequest
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random

@Transactional
class LoggingEventServiceIT : BaseIntegrationTest() {

    @Test
    fun `should log to database using valtimo-database-appender-xml`() {
        val now = LocalDateTime.now()
        saveLoggingEvent("event 1", now - Duration.ofDays(1))
        val searchRequest = LoggingEventSearchRequest(olderThanTimestamp = now - Duration.ofDays(2))
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

    private fun saveLoggingEvent(message: String, localDateTime: LocalDateTime) {
        val timestamp = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        loggingEventRepository.save(
            LoggingEvent(
                id = Random.nextLong(),
                timestamp = timestamp,
                formattedMessage = message,
                loggerName = "loggerName",
                level = "level",
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
}