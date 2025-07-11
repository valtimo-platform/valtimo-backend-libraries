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

import com.ritense.iko.service.IkoTabService
import com.ritense.tab.web.rest.dto.TabDto
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@SkipComponentScan
@RequestMapping("/api", produces = [ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE])
class IkoTabResource(
    private val ikoTabService: IkoTabService
) {

    @GetMapping("/v1/iko-data-aggregate/{ikoDataAggregateKey}/tab")
    fun getIkoTabs(
        @PathVariable ikoDataAggregateKey: String,
    ): ResponseEntity<List<TabDto>> {
        return ResponseEntity.ok(
            ikoTabService.findAllTabsByIkoDataAggregateKey(ikoDataAggregateKey).map { TabDto.from(it) }
        )
    }

}