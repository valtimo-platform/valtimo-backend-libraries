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
import com.ritense.iko.service.IkoDataRequestService
import com.ritense.iko.web.rest.request.IkoDataRequestCreateRequest
import com.ritense.iko.web.rest.request.IkoDataRequestUpdateRequest
import com.ritense.iko.web.rest.response.IkoDataRequestResponse
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valtimo.contract.iko.PropertyField
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@SkipComponentScan
@RequestMapping("/api/management", produces = [APPLICATION_JSON_UTF8_VALUE])
class IkoDataRequestManagementResource(
    private val service: IkoDataRequestService,
    private val ikoDataAggregateService: IkoDataAggregateService,
) {

    @RunWithoutAuthorization
    @GetMapping("/v1/iko-property-fields/{type}/data-request")
    fun getIkoRepositoryConfigPropertyFields(
        @PathVariable type: String,
    ): ResponseEntity<List<PropertyField>> {
        return ResponseEntity.ok(service.getIkoDataRequestPropertyFields(type))
    }

    @RunWithoutAuthorization
    @GetMapping("/v1/iko-data-aggregate/{ikoDataAggregateKey}/data-request")
    fun getIkoDataRequestsForManagement(
        @PathVariable ikoDataAggregateKey: String,
    ): ResponseEntity<List<IkoDataRequestResponse>> {
        val ikoDataRequests = service.findAll(
            ikoDataAggregateKey = ikoDataAggregateKey,
        )
        return ResponseEntity.ok(ikoDataRequests.map { IkoDataRequestResponse.from(it) })
    }

    @RunWithoutAuthorization
    @GetMapping("/v1/iko-data-aggregate/{ikoDataAggregateKey}/data-request/{key}")
    fun getIkoDataRequest(
        @PathVariable ikoDataAggregateKey: String,
        @PathVariable key: String,
    ): ResponseEntity<IkoDataRequestResponse> {
        val ikoDataRequest = service.getByKey(key, ikoDataAggregateKey)
        return ResponseEntity.ok(IkoDataRequestResponse.from(ikoDataRequest))
    }


    @RunWithoutAuthorization
    @PostMapping("/v1/iko-data-aggregate/{ikoDataAggregateKey}/data-request/{key}")
    fun createIkoDataRequest(
        @PathVariable ikoDataAggregateKey: String,
        @PathVariable key: String,
        @RequestBody request: IkoDataRequestCreateRequest
    ): ResponseEntity<IkoDataRequestResponse> {
        val ikoDataAggregate = ikoDataAggregateService.getByKey(ikoDataAggregateKey)
        val existingIkoDataRequests = service.findAll(
            ikoDataAggregateKey = ikoDataAggregateKey,
        )
        val ikoDataRequest = service.create(
            ikoDataRequest = request.toEntity(key, ikoDataAggregate, existingIkoDataRequests.size)
        )
        return ResponseEntity.ok(IkoDataRequestResponse.from(ikoDataRequest))
    }

    @RunWithoutAuthorization
    @PutMapping("/v1/iko-data-aggregate/{ikoDataAggregateKey}/data-request/{key}")
    fun updateIkoDataRequest(
        @PathVariable ikoDataAggregateKey: String,
        @PathVariable key: String,
        @RequestBody request: IkoDataRequestUpdateRequest,
    ): ResponseEntity<IkoDataRequestResponse> {
        require(request.key == key)
        val existingIkoDataRequest = service.getByKey(ikoDataAggregateKey = ikoDataAggregateKey, key = key)
        val ikoDataRequest = service.update(
            ikoDataRequest = request.toEntity(
                existingIkoDataRequest.id.ikoDataAggregate,
                existingIkoDataRequest.order,
            ),
        )
        return ResponseEntity.ok(IkoDataRequestResponse.from(ikoDataRequest))
    }

    @RunWithoutAuthorization
    @PutMapping("/v1/iko-data-aggregate/{ikoDataAggregateKey}/data-request")
    fun updateIkoDataRequestsOrder(
        @PathVariable ikoDataAggregateKey: String,
        @RequestBody request: List<IkoDataRequestUpdateRequest>,
    ): ResponseEntity<List<IkoDataRequestResponse>> {
        val existingIkoDataRequests = service.findAll(
            ikoDataAggregateKey = ikoDataAggregateKey,
        )
        require(request.map { it.key }.toSet() == existingIkoDataRequests.map { it.id.key }.toSet())
        val ikoDataRequests = request.mapIndexed { index, updatedDataRequest ->
            val existingDataRequest = existingIkoDataRequests.first { it.id.key == updatedDataRequest.key }
            service.update(
                ikoDataRequest = existingDataRequest.copy(order = index),
            )
        }
        return ResponseEntity.ok(ikoDataRequests.map { IkoDataRequestResponse.from(it) })
    }

    @RunWithoutAuthorization
    @DeleteMapping("/v1/iko-data-aggregate/{ikoDataAggregateKey}/data-request/{key}")
    fun deleteIkoDataRequest(
        @PathVariable ikoDataAggregateKey: String,
        @PathVariable key: String,
    ): ResponseEntity<IkoDataRequestResponse> {
        service.delete(key, ikoDataAggregateKey)
        return ResponseEntity.noContent().build()
    }
}
