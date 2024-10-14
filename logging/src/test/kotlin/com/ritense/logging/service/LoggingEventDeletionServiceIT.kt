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
import com.ritense.logging.repository.LoggingEventSpecificationHelper.Companion.byFormattedMessage
import jakarta.transaction.Transactional
import mu.KLogger
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random

@Transactional
class LoggingEventDeletionServiceIT : BaseIntegrationTest() {

    @Test
    fun `should log to database using valtimo-database-appender-xml`() {
        saveLoggingEvent("log line old", LocalDateTime.now() - Duration.ofDays(356))
        saveLoggingEvent("log line new", LocalDateTime.now())

        loggingEventDeletionService.deleteOldLoggingEvents()

        assertEquals(0, loggingEventRepository.count(byFormattedMessage("log line old")))
        assertEquals(1, loggingEventRepository.count(byFormattedMessage("log line new")))
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