/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.dataprovider.web.rest

import com.ritense.dataprovider.service.DataProviderService
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class DataProviderResource(
    private val dataProviderService: DataProviderService,
) {

    @GetMapping("/v1/data/{category}/provider")
    fun getProviderNames(
        @PathVariable category: String,
    ): ResponseEntity<List<String>> {
        return ResponseEntity.ok(dataProviderService.getProviderNames(category))
    }

    @GetMapping("/v1/data/{category}/all")
    fun getAll(
        @PathVariable category: String,
        @RequestParam provider: String?,
        @RequestParam query: Map<String, Any> = emptyMap()
    ): ResponseEntity<List<Any>> {
        return ResponseEntity.ok(dataProviderService.getAllData(category, provider, query))
    }

    @GetMapping("/v1/data/{category}")
    fun getData(
        @PathVariable category: String,
        @RequestParam provider: String?,
        @RequestParam query: Map<String, Any> = emptyMap()
    ): ResponseEntity<Any?> {
        return ResponseEntity.ok(dataProviderService.getData(category, provider, query))
    }

    @PostMapping("/v1/data/{category}")
    fun postData(
        @PathVariable category: String,
        @RequestParam provider: String?,
        @RequestParam query: Map<String, Any>,
        @RequestBody data: Any?
    ): ResponseEntity<Unit> {
        dataProviderService.postData(category, provider, query, data)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/v1/data/{category}")
    fun deleteData(
        @PathVariable category: String,
        @RequestParam provider: String?,
        @RequestParam query: Map<String, Any>,
        @RequestBody data: Any?
    ): ResponseEntity<Unit> {
        dataProviderService.deleteData(category, provider, query, data)
        return ResponseEntity.noContent().build()
    }
}
