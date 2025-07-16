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

import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.iko.service.IkoDataAggregateService
import com.ritense.iko.web.rest.request.IkoDataAggregateCreateRequest
import com.ritense.iko.web.rest.request.IkoDataAggregateUpdateRequest
import com.ritense.iko.web.rest.response.IkoDataAggregateListResponse
import com.ritense.iko.web.rest.response.IkoDataAggregateResponse
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valtimo.contract.iko.PropertyField
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@SkipComponentScan
@RequestMapping("/api/management", produces = [APPLICATION_JSON_UTF8_VALUE])
class IkoDataAggregateManagementResource(
    private val service: IkoDataAggregateService,
) {

    @RunWithoutAuthorization
    @GetMapping("/v1/iko-property-fields/{type}/data-aggregate")
    fun getIkoDataAggregatePropertyFields(
        @PathVariable type: String,
    ): ResponseEntity<List<PropertyField>> {
        return ResponseEntity.ok(service.getIkoDataAggregatePropertyFields(type))
    }

    @RunWithoutAuthorization
    @GetMapping("/v1/iko-data-aggregate")
    fun getIkoDataAggregatesForManagement(
        @RequestParam key: String?,
        @RequestParam title: String?,
        @PageableDefault(sort = ["title"], direction = ASC) pageable: Pageable
    ): ResponseEntity<Page<IkoDataAggregateListResponse>> {
        val ikoDataAggregates = service.findAll(
            key = key,

            title = title,
            pageable = pageable
        )
        return ResponseEntity.ok(ikoDataAggregates.map { IkoDataAggregateListResponse.from(it) })
    }

    @RunWithoutAuthorization
    @GetMapping("/v1/iko-data-aggregate/{key}")
    fun getIkoDataAggregate(
        @PathVariable key: String,
    ): ResponseEntity<IkoDataAggregateResponse> {
        val ikoDataAggregate = service.getByKey(key)
        return ResponseEntity.ok(IkoDataAggregateResponse.from(ikoDataAggregate))
    }

    @RunWithoutAuthorization
    @PostMapping("/v1/iko-data-aggregate/{key}")
    fun createIkoDataAggregate(
        @PathVariable key: String,
        @RequestBody request: IkoDataAggregateCreateRequest
    ): ResponseEntity<IkoDataAggregateResponse> {
        val ikoDataAggregate = service.createIkoDataAggregate(
            key = key,
            ikoRepositoryConfigKey = request.ikoRepositoryConfigKey,
            title = request.title,
            properties = request.properties,
        )
        return ResponseEntity.ok(IkoDataAggregateResponse.from(ikoDataAggregate))
    }

    @RunWithoutAuthorization
    @PutMapping("/v1/iko-data-aggregate/{key}")
    fun updateIkoDataAggregate(
        @PathVariable key: String,
        @RequestBody request: IkoDataAggregateUpdateRequest
    ): ResponseEntity<IkoDataAggregateResponse> {
        val ikoDataAggregate = service.saveIkoDataAggregate(
            key = key,
            title = request.title,
            ikoRepositoryConfigKey = request.ikoRepositoryConfigKey,
            properties = request.properties,
        )
        return ResponseEntity.ok(IkoDataAggregateResponse.from(ikoDataAggregate))
    }

    @RunWithoutAuthorization
    @DeleteMapping("/v1/iko-data-aggregate/{key}")
    fun deleteIkoDataAggregate(
        @PathVariable key: String,
    ): ResponseEntity<IkoDataAggregateResponse> {
        service.deleteIkoDataAggregate(key)
        return ResponseEntity.noContent().build()
    }
}
