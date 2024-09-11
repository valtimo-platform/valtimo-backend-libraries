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
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime
import java.time.ZoneId

class LoggingEventSpecificationHelper {

    companion object {

        const val CALLER_CLASS: String = "callerClass"
        const val FORMATTED_MESSAGE: String = "formattedMessage"
        const val LEVEL: String = "level"
        const val TIMESTAMP: String = "timestamp"

        @JvmStatic
        fun byFormattedMessage(formattedMessage: String) = Specification<LoggingEvent> { root, _, cb ->
            cb.equal(root.get<String>(FORMATTED_MESSAGE), formattedMessage)
        }

        @JvmStatic
        fun byLevel(level: String) = Specification<LoggingEvent> { root, _, cb ->
            cb.equal(root.get<String>(LEVEL), level)
        }

        @JvmStatic
        fun byCallerClass(callerClass: Class<*>) = Specification<LoggingEvent> { root, _, cb ->
            cb.equal(root.get<String>(CALLER_CLASS), callerClass.name)
        }

        @JvmStatic
        fun byOlderThan(localDateTime: LocalDateTime) = Specification<LoggingEvent> { root, _, cb ->
            val timestamp = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            cb.lessThan(root[TIMESTAMP], timestamp)
        }

    }
}