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

import com.ritense.logging.domain.LoggingEvent
import com.ritense.logging.repository.LoggingEventRepository
import com.ritense.logging.repository.LoggingEventSpecificationHelper.Companion.byLikeFormattedMessage
import com.ritense.logging.repository.LoggingEventSpecificationHelper.Companion.byMinimumLevel
import com.ritense.logging.repository.LoggingEventSpecificationHelper.Companion.byNewerThan
import com.ritense.logging.repository.LoggingEventSpecificationHelper.Companion.byOlderThan
import com.ritense.logging.repository.LoggingEventSpecificationHelper.Companion.byProperty
import com.ritense.logging.repository.LoggingEventSpecificationHelper.Companion.query
import com.ritense.logging.web.rest.dto.LoggingEventSearchRequest
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@SkipComponentScan
class LoggingEventService(
    private val loggingEventRepository: LoggingEventRepository
) {

    @Transactional(readOnly = true)
    fun searchLoggingEvents(searchRequest: LoggingEventSearchRequest, pageable: Pageable): Page<LoggingEvent> {
        var spec = query()

        if (searchRequest.afterTimestamp != null) {
            spec = spec.and(byNewerThan(searchRequest.afterTimestamp))
        }
        if (searchRequest.beforeTimestamp != null) {
            spec = spec.and(byOlderThan(searchRequest.beforeTimestamp))
        }
        if (searchRequest.level != null) {
            spec = spec.and(byMinimumLevel(searchRequest.level))
        }
        if (searchRequest.likeFormattedMessage != null) {
            spec = spec.and(byLikeFormattedMessage(searchRequest.likeFormattedMessage))
        }
        searchRequest.properties.forEach { (key, value) ->
            spec = spec.and(byProperty(key, value))
        }

        return loggingEventRepository.findAll(spec, pageable)
    }
}