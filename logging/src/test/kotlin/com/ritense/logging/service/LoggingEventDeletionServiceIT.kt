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
import com.ritense.logging.repository.LoggingEventPropertySpecificationHelper.Companion.byKeyValue
import com.ritense.logging.repository.LoggingEventSpecificationHelper.Companion.byFormattedMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random

@Transactional
class LoggingEventDeletionServiceIT : BaseIntegrationTest() {

    @Test
    fun `should log to database using valtimo-database-appender-xml`() {
        val eventOld = saveLoggingEvent("log line old", LocalDateTime.now() - Duration.ofDays(356))
        saveLoggingEventProp(eventOld, "old key", "old value")
        val eventNew = saveLoggingEvent("log line new")
        saveLoggingEventProp(eventNew, "new key", "new value")

        loggingEventDeletionService.deleteOldLoggingEvents()

        assertEquals(0, loggingEventRepository.count(byFormattedMessage("log line old")))
        assertEquals(1, loggingEventRepository.count(byFormattedMessage("log line new")))
        assertEquals(0, loggingEventPropertyRepository.count(byKeyValue("old key", "old value")))
        assertEquals(1, loggingEventPropertyRepository.count(byKeyValue("new key", "new value")))
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