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
import com.ritense.iko.service.IkoConnectorService
import com.ritense.iko.web.rest.request.IkoConnectorConfigCreateRequest
import com.ritense.iko.web.rest.request.IkoConnectorConfigUpdateRequest
import com.ritense.iko.web.rest.response.IkoConnectorConfigListResponse
import com.ritense.iko.web.rest.response.IkoConnectorConfigResponse
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
class IkoConnectorManagementResource(
    private val service: IkoConnectorService,
) {

    @RunWithoutAuthorization
    @GetMapping("/v1/iko-types")
    fun getIkoConnectorsTypes(): ResponseEntity<List<String>> {
        return ResponseEntity.ok(service.getIkoConnectorTypes())
    }

    @RunWithoutAuthorization
    @GetMapping("/v1/iko-property-fields/{type}/connector-config")
    fun getIkoConnectorPropertyFields(
        @PathVariable type: String,
    ): ResponseEntity<List<PropertyField>> {
        return ResponseEntity.ok(service.getIkoConnectorPropertyFields(type))
    }

    @RunWithoutAuthorization
    @GetMapping("/v1/iko")
    fun getIkoConnectorConfigsForManagement(
        @RequestParam title: String?,
        @RequestParam type: String?,
        @PageableDefault(sort = ["title"], direction = ASC) pageable: Pageable
    ): ResponseEntity<Page<IkoConnectorConfigListResponse>> {
        val ikoConnectorConfigs = service.findAll(
            title = title,
            type = type,
            pageable = pageable
        )
        return ResponseEntity.ok(ikoConnectorConfigs.map { IkoConnectorConfigListResponse.from(it) })
    }

    @RunWithoutAuthorization
    @GetMapping("/v1/iko/{key}")
    fun getIkoConnectorConfig(
        @PathVariable key: String,
    ): ResponseEntity<IkoConnectorConfigResponse> {
        val ikoConnectorConfig = service.getByKey(key)
        return ResponseEntity.ok(IkoConnectorConfigResponse.from(ikoConnectorConfig))
    }

    @RunWithoutAuthorization
    @PostMapping("/v1/iko/{key}")
    fun createIkoConnectorConfig(
        @PathVariable key: String,
        @RequestBody request: IkoConnectorConfigCreateRequest
    ): ResponseEntity<IkoConnectorConfigResponse> {
        val ikoConnectorConfig = service.createIkoConnectorConfig(
            key = key,
            title = request.title,
            type = request.type,
            properties = request.properties,
        )
        return ResponseEntity.ok(IkoConnectorConfigResponse.from(ikoConnectorConfig))
    }

    @RunWithoutAuthorization
    @PutMapping("/v1/iko/{key}")
    fun updateIkoConnectorConfig(
        @PathVariable key: String,
        @RequestBody request: IkoConnectorConfigUpdateRequest
    ): ResponseEntity<IkoConnectorConfigResponse> {
        val ikoConnectorConfig = service.updateIkoConnectorConfig(
            key = key,
            title = request.title,
            type = request.type,
            properties = request.properties,
        )
        return ResponseEntity.ok(IkoConnectorConfigResponse.from(ikoConnectorConfig))
    }

    @RunWithoutAuthorization
    @DeleteMapping("/v1/iko/{key}")
    fun deleteIkoConnectorConfig(
        @PathVariable key: String,
    ): ResponseEntity<IkoConnectorConfigResponse> {
        service.deleteIkoConnectorConfig(key)
        return ResponseEntity.noContent().build()
    }
}
