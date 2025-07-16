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
import com.ritense.iko.service.IkoRepositoryService
import com.ritense.iko.web.rest.request.IkoRepositoryConfigCreateRequest
import com.ritense.iko.web.rest.request.IkoRepositoryConfigUpdateRequest
import com.ritense.iko.web.rest.response.IkoRepositoryConfigListResponse
import com.ritense.iko.web.rest.response.IkoRepositoryConfigResponse
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
class IkoRepositoryManagementResource(
    private val service: IkoRepositoryService,
) {

    @RunWithoutAuthorization
    @GetMapping("/v1/iko-types")
    fun getIkoRepositoriesTypes(): ResponseEntity<List<String>> {
        return ResponseEntity.ok(service.getIkoRepositoryTypes())
    }

    @RunWithoutAuthorization
    @GetMapping("/v1/iko-property-fields/{type}/repository-config")
    fun getIkoRepositoryConfigPropertyFields(
        @PathVariable type: String,
    ): ResponseEntity<List<PropertyField>> {
        return ResponseEntity.ok(service.getIkoRepositoryConfigPropertyFields(type))
    }

    @RunWithoutAuthorization
    @GetMapping("/v1/iko")
    fun getIkoRepositoryConfigsForManagement(
        @RequestParam title: String?,
        @RequestParam type: String?,
        @PageableDefault(sort = ["title"], direction = ASC) pageable: Pageable
    ): ResponseEntity<Page<IkoRepositoryConfigListResponse>> {
        val ikoRepositoryConfigs = service.findAll(
            title = title,
            type = type,
            pageable = pageable
        )
        return ResponseEntity.ok(ikoRepositoryConfigs.map { IkoRepositoryConfigListResponse.from(it) })
    }

    @RunWithoutAuthorization
    @GetMapping("/v1/iko/{key}")
    fun getIkoRepositoryConfig(
        @PathVariable key: String,
    ): ResponseEntity<IkoRepositoryConfigResponse> {
        val ikoRepositoryConfig = service.getByKey(key)
        return ResponseEntity.ok(IkoRepositoryConfigResponse.from(ikoRepositoryConfig))
    }

    @RunWithoutAuthorization
    @PostMapping("/v1/iko/{key}")
    fun createIkoRepositoryConfig(
        @PathVariable key: String,
        @RequestBody request: IkoRepositoryConfigCreateRequest
    ): ResponseEntity<IkoRepositoryConfigResponse> {
        val ikoRepositoryConfig = service.createIkoRepositoryConfig(
            key = key,
            title = request.title,
            type = request.type,
            properties = request.properties,
        )
        return ResponseEntity.ok(IkoRepositoryConfigResponse.from(ikoRepositoryConfig))
    }

    @RunWithoutAuthorization
    @PutMapping("/v1/iko/{key}")
    fun updateIkoRepositoryConfig(
        @PathVariable key: String,
        @RequestBody request: IkoRepositoryConfigUpdateRequest
    ): ResponseEntity<IkoRepositoryConfigResponse> {
        val ikoRepositoryConfig = service.saveIkoRepositoryConfig(
            key = key,
            title = request.title,
            type = request.type,
            properties = request.properties,
        )
        return ResponseEntity.ok(IkoRepositoryConfigResponse.from(ikoRepositoryConfig))
    }

    @RunWithoutAuthorization
    @DeleteMapping("/v1/iko/{key}")
    fun deleteIkoRepositoryConfig(
        @PathVariable key: String,
    ): ResponseEntity<IkoRepositoryConfigResponse> {
        service.deleteIkoRepositoryConfig(key)
        return ResponseEntity.noContent().build()
    }
}
