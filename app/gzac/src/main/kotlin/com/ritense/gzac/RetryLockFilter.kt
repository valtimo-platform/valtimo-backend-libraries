/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.gzac

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.classic.spi.ThrowableProxy
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.core.spi.FilterReply
import org.slf4j.MDC
import org.slf4j.Marker


class RetryLockFilter(): TurboFilter() {

    override fun decide(
        marker: Marker?,
        logger: Logger?,
        level: Level?,
        format: String?,
        params: Array<out Any?>?,
        throwable: Throwable?
    ): FilterReply {
        if (logger == null || level !== Level.ERROR) return FilterReply.NEUTRAL
        val name: String? = logger.getName()
        if (name == null || !name.startsWith("org.operaton")) return FilterReply.NEUTRAL

        val lastArgument = params?.lastOrNull()
        if (lastArgument !is Throwable) return FilterReply.NEUTRAL

        // Optional MDC read (only now)
        val last = MDC.get("operaton.jobLastAttempt")
        if ("true" == last) return FilterReply.NEUTRAL

        // Match specific exception
        if (lastArgument.cause?.javaClass?.getName() != "org.springframework.orm.ObjectOptimisticLockingFailureException") return FilterReply.NEUTRAL

        // Create a new event (WARN instead of ERROR, with same throwable & args)
        val e: LoggingEvent = LoggingEvent()
        e.setLoggerName(logger.getName())
        e.setLevel(Level.WARN) // change level
        e.setMessage("This error was removed") // change message
        e.setArgumentArray(params)
        if (throwable != null) {
            e.setThrowableProxy(ThrowableProxy(throwable))
        }
        e.setMDCPropertyMap(MDC.getCopyOfContextMap())
        e.setTimeStamp(System.currentTimeMillis())
        e.setThreadName(Thread.currentThread().getName())

        // Emit the rewritten event and kill the original
        logger.callAppenders(e)

        // Rewrite or deny here
        return FilterReply.DENY
    }

}