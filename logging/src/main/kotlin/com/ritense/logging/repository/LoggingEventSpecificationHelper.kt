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

package com.ritense.logging.repository

import com.ritense.logging.domain.LoggingEvent
import com.ritense.logging.repository.LoggingEventPropertySpecificationHelper.Companion.ID
import com.ritense.logging.repository.LoggingEventPropertySpecificationHelper.Companion.KEY
import com.ritense.logging.repository.LoggingEventPropertySpecificationHelper.Companion.VALUE
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime
import java.time.ZoneId

class LoggingEventSpecificationHelper {

    companion object {

        const val CALLER_CLASS: String = "callerClass"
        const val FORMATTED_MESSAGE: String = "formattedMessage"
        const val LEVEL: String = "level"
        const val TIMESTAMP: String = "timestamp"
        const val PROPERTIES: String = "properties"
        val LOG_LEVELS: List<String> = listOf("TRACE", "DEBUG", "INFO", "WARN", "ERROR")

        @JvmStatic
        fun query() = Specification<LoggingEvent> { _, _, cb ->
            cb.equal(cb.literal(1), 1)
        }

        @JvmStatic
        fun byFormattedMessage(formattedMessage: String) = Specification<LoggingEvent> { root, _, cb ->
            cb.equal(root.get<String>(FORMATTED_MESSAGE), formattedMessage)
        }

        @JvmStatic
        fun byLikeFormattedMessage(likeFormattedMessage: String) = Specification<LoggingEvent> { root, _, cb ->
            cb.like(root[FORMATTED_MESSAGE], "%$likeFormattedMessage%")
        }

        @JvmStatic
        fun byLevel(level: String) = Specification<LoggingEvent> { root, _, cb ->
            cb.equal(root.get<String>(LEVEL), level.uppercase())
        }

        @JvmStatic
        fun byMinimumLevel(minimumLevel: String) = Specification<LoggingEvent> { root, _, _ ->
            val levels = LOG_LEVELS.subList(LOG_LEVELS.indexOf(minimumLevel.uppercase()), LOG_LEVELS.size)
            root.get<String>(LEVEL).`in`(levels)
        }

        @JvmStatic
        fun byCallerClass(callerClass: Class<*>) = Specification<LoggingEvent> { root, _, cb ->
            cb.equal(root.get<String>(CALLER_CLASS), callerClass.name)
        }

        @JvmStatic
        fun byNewerThan(localDateTime: LocalDateTime) = Specification<LoggingEvent> { root, _, cb ->
            val timestamp = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            cb.greaterThan(root[TIMESTAMP], timestamp)
        }

        @JvmStatic
        fun byOlderThan(localDateTime: LocalDateTime) = Specification<LoggingEvent> { root, _, cb ->
            val timestamp = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            cb.lessThan(root[TIMESTAMP], timestamp)
        }

        @JvmStatic
        fun byProperty(key: String, value: String) = Specification<LoggingEvent> { root, _, cb ->
            val properties = root.join<Any, Any>(PROPERTIES)
            cb.and(
                cb.equal(properties.get<Any>(ID).get<String>(KEY), key),
                cb.equal(properties.get<String>(VALUE), value)
            )
        }

    }
}