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

package com.ritense.logging.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType.LAZY
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.springframework.data.annotation.Immutable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Immutable
@Entity
@Table(name = "logging_event")
class LoggingEvent(

    @Id
    @Column(name = "event_id", updatable = false)
    val id: Long,

    @Column(name = "timestmp", updatable = false)
    val timestamp: Long,

    @Column(name = "formatted_message", updatable = false)
    val formattedMessage: String,

    @Column(name = "logger_name", updatable = false)
    val loggerName: String,

    @Column(name = "level_string", updatable = false)
    val level: String,

    @Column(name = "thread_name", updatable = false)
    val threadName: String?,

    @Column(name = "reference_flag", updatable = false)
    val referenceFlag: Short?,

    @Column(name = "arg0", updatable = false)
    val arg0: String?,

    @Column(name = "arg1", updatable = false)
    val arg1: String?,

    @Column(name = "arg2", updatable = false)
    val arg2: String?,

    @Column(name = "arg3", updatable = false)
    val arg3: String?,

    @Column(name = "caller_filename", updatable = false)
    val callerFilename: String,

    @Column(name = "caller_class", updatable = false)
    val callerClass: String,

    @Column(name = "caller_method", updatable = false)
    val callerMethod: String,

    @Column(name = "caller_line", updatable = false)
    val callerLine: Short,

    @OneToMany(fetch = LAZY, mappedBy = "event")
    val properties: List<LoggingEventProperty>,

    @OrderBy("id.i")
    @OneToMany(fetch = LAZY, mappedBy = "event")
    val exceptions: List<LoggingEventException>,
) {
    fun getStacktrace(): String? {
        return if (exceptions.isEmpty()) {
            null
        } else {
            exceptions.joinToString("\n") { it.traceLine }
        }
    }

    fun getTimestampLocalDateTime() = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
}