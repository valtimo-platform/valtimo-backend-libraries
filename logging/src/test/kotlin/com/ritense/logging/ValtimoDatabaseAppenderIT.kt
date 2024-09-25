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

package com.ritense.logging

import com.ritense.logging.repository.LoggingEventSpecificationHelper.Companion.byCallerClass
import com.ritense.logging.repository.LoggingEventSpecificationHelper.Companion.byLevel
import com.ritense.logging.repository.LoggingEventSpecificationHelper.Companion.byLikeFormattedMessage
import mu.KLogger
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.transaction.annotation.Transactional

@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValtimoDatabaseAppenderIT : BaseIntegrationTest() {

    @BeforeAll
    fun beforeAll() {
        loggingEventPropertyRepository.deleteAll()
        loggingEventExceptionRepository.deleteAll()
        loggingEventRepository.deleteAll()
    }

    @Test
    fun `should log to database using valtimo-database-appender-xml`() {
        withLoggingContext("Test key" to "Test value") {
            logger.error(IllegalStateException("Test exception")) { "Test error message" }
        }
        logger.info { "Test info message" }

        val spec = byCallerClass(ValtimoDatabaseAppenderIT::class.java)
        val errorEvent = loggingEventRepository.findOne(spec.and(byLevel("ERROR"))).orElseThrow()
        val infoEvent = loggingEventRepository.findOne(spec.and(byLevel("INFO"))).orElseThrow()
        val debugEventCount = loggingEventRepository.count(byLevel("DEBUG"))

        assertEquals("Test error message", errorEvent.formattedMessage)
        assertEquals(1, errorEvent.properties.size)
        assertEquals("Test key", errorEvent.properties[0].getKey())
        assertEquals("Test value", errorEvent.properties[0].value)
        assertEquals("java.lang.IllegalStateException: Test exception", errorEvent.exceptions[0].traceLine)
        assertEquals("Test info message", infoEvent.formattedMessage)
        assertEquals(0, infoEvent.properties.size)
        assertEquals(0, infoEvent.exceptions.size)
        assertEquals(0, debugEventCount)
    }

    @Test
    fun `should log very long trace_line`() {
        val veryLongExceptionMessage = "Very long exception message: ${(1..1000).joinToString(" ")}"
        logger.error(IllegalStateException("Test exception")) { veryLongExceptionMessage }

        val spec = byCallerClass(ValtimoDatabaseAppenderIT::class.java)
            .and(byLikeFormattedMessage("Very long exception message"))
        val errorEvent = loggingEventRepository.findOne(spec).orElseThrow()

        assertEquals(veryLongExceptionMessage, errorEvent.formattedMessage)
        assertEquals(3921, veryLongExceptionMessage.length)
    }

    private companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}