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

package com.ritense.logging.web.rest.dto

import java.time.LocalDateTime

data class LoggingEventSearchRequest(
    val afterTimestamp: LocalDateTime? = null,
    val beforeTimestamp: LocalDateTime? = null,
    val level: String? = null,
    val likeFormattedMessage: String? = null,
    val properties: List<LoggingEventPropertyDto> = emptyList(),
)