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

package com.ritense.logging.web.rest

import com.ritense.logging.service.LoggingEventService
import com.ritense.logging.web.rest.dto.LoggingEventResponse
import com.ritense.logging.web.rest.dto.LoggingEventSearchRequest
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SkipComponentScan
@RequestMapping("/api/management", produces = [APPLICATION_JSON_UTF8_VALUE])
class LoggingEventManagementResource(
    private val loggingEventService: LoggingEventService,
) {

    @Transactional(readOnly = true)
    @PostMapping("/v1/logging")
    fun searchLoggingEvents(
        @RequestBody searchRequest: LoggingEventSearchRequest,
        @PageableDefault(sort = ["timestamp"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ResponseEntity<Page<LoggingEventResponse>> {
        val loggingEvents = loggingEventService.searchLoggingEvents(searchRequest, pageable)
        val sorted = LoggingEventResponse.of(loggingEvents.content)
        return ResponseEntity.ok(PageImpl(sorted, pageable, loggingEvents.totalElements))
    }

}
