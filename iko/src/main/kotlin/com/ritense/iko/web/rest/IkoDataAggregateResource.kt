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

package com.ritense.iko.web.rest

import com.ritense.iko.service.IkoDataAggregateService
import com.ritense.iko.web.rest.response.IkoDataAggregateListResponse
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@SkipComponentScan
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class IkoDataAggregateResource(
    private val service: IkoDataAggregateService,
) {

    @GetMapping("/v1/iko-data-aggregate")
    fun getIkoDataAggregates(
        @RequestParam key: String?,
        @RequestParam title: String?,
        @PageableDefault(size = 10000, sort = ["title"], direction = ASC) pageable: Pageable
    ): ResponseEntity<Page<IkoDataAggregateListResponse>> {
        val ikoDataAggregates = service.findAll(
            key = key,
            title = title,
            pageable = pageable,
        )
        return ResponseEntity.ok(ikoDataAggregates.map { IkoDataAggregateListResponse.from(it) })
    }
}
