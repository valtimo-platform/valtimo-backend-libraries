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

import ch.qos.logback.classic.Logger as LogbackLogger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.slf4j.MDC

class ResourceLoggerTest {

    lateinit var listAppender: ListAppender<ILoggingEvent>

    @BeforeEach
    fun setUp() {
        val logger = LoggerFactory.getLogger(LogResource::class.java) as LogbackLogger

        listAppender = ListAppender<ILoggingEvent>()
        listAppender.start();

        logger.addAppender(listAppender)
    }

    @Test
    fun `should log resource`() {
        ResourceLogger.withResource(this::class.java, "key") {
            LogResource().outputMessage()
        }

        assertEquals(0, MDC.getCopyOfContextMap().size)
        assertEquals(1, listAppender.list.size)
        assertEquals(1, listAppender.list[0].mdcPropertyMap.size)
        assertEquals("key", listAppender.list[0].mdcPropertyMap["LoggingResourceHandlerTest"])
    }

    @Test
    fun `should log resource with context in method`() {
        LogResource().logSomething()

        assertEquals(0, MDC.getCopyOfContextMap().size)
        assertEquals(1, listAppender.list.size)
        assertEquals(1, listAppender.list[0].mdcPropertyMap.size)
        assertEquals("some-key", listAppender.list[0].mdcPropertyMap["LogResource"])
    }

    @Test
    fun `should log resource with multiple levels`() {
        LogResource().logSomethingWithTwoLevelsOfMetaData()

        assertEquals(0, MDC.getCopyOfContextMap().size)
        assertEquals(1, listAppender.list.size)
        assertEquals(2, listAppender.list[0].mdcPropertyMap.size)
        assertEquals("some-key", listAppender.list[0].mdcPropertyMap["LogResource"])
        assertEquals("second-key", listAppender.list[0].mdcPropertyMap["InnerClass"])
    }

    @Test
    fun `should nog log siblings if context block has been closed`() {
        LogResource().logSomethingWithSiblingResource()

        assertEquals(0, MDC.getCopyOfContextMap().size)
        assertEquals(1, listAppender.list.size)
        assertEquals(1, listAppender.list[0].mdcPropertyMap.size)
        assertEquals("some-key", listAppender.list[0].mdcPropertyMap["LogResource"])
    }

}