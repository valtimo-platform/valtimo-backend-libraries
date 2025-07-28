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
import com.ritense.iko.service.IkoListColumnService
import com.ritense.iko.web.rest.request.IkoListColumnCreateRequest
import com.ritense.iko.web.rest.request.IkoListColumnUpdateRequest
import com.ritense.search.importer.ListColumnDto
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
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
class IkoListColumnManagementResource(
    private val service: IkoListColumnService,
) {

    @RunWithoutAuthorization
    @GetMapping("/v1/iko-data-aggregate/{ikoDataAggregateKey}/column")
    fun getIkoListColumnsForManagement(
        @PathVariable ikoDataAggregateKey: String,
    ): ResponseEntity<List<ListColumnDto>> {
        val ikoListColumns = service.findAllColumnsByIkoDataAggregateKey(
            ikoDataAggregateKey = ikoDataAggregateKey,
        )
        return ResponseEntity.ok(ikoListColumns.map { ListColumnDto.from(it) })
    }

    @RunWithoutAuthorization
    @GetMapping("/v1/iko-data-aggregate/{ikoDataAggregateKey}/column/{key}")
    fun getIkoListColumn(
        @PathVariable ikoDataAggregateKey: String,
        @PathVariable key: String,
    ): ResponseEntity<ListColumnDto> {
        val ikoListColumn = service.getByKey(ikoDataAggregateKey, key)
        return ResponseEntity.ok(ListColumnDto.from(ikoListColumn))
    }

    @RunWithoutAuthorization
    @PostMapping("/v1/iko-data-aggregate/{ikoDataAggregateKey}/column/{key}")
    fun createIkoListColumn(
        @PathVariable ikoDataAggregateKey: String,
        @PathVariable key: String,
        @RequestBody request: IkoListColumnCreateRequest
    ): ResponseEntity<ListColumnDto> {
        val existingIkoListColumns = service.findAllColumnsByIkoDataAggregateKey(
            ikoDataAggregateKey = ikoDataAggregateKey,
        )
        val ikoListColumn = service.create(
            ikoDataAggregateKey = ikoDataAggregateKey,
            listColumn = request.toEntity(ikoDataAggregateKey, existingIkoListColumns.size)
        )
        return ResponseEntity.ok(ListColumnDto.from(ikoListColumn))
    }

    @RunWithoutAuthorization
    @PutMapping("/v1/iko-data-aggregate/{ikoDataAggregateKey}/column/{key}")
    fun updateIkoListColumn(
        @PathVariable ikoDataAggregateKey: String,
        @PathVariable key: String,
        @RequestBody request: IkoListColumnUpdateRequest,
    ): ResponseEntity<ListColumnDto> {
        require(request.key == key)
        val existingIkoListColumn = service.findByKey(ikoDataAggregateKey, key)
        requireNotNull(existingIkoListColumn)
        val ikoListColumn = service.update(
            ikoDataAggregateKey = ikoDataAggregateKey,
            listColumn = request.toEntity(existingIkoListColumn.id, ikoDataAggregateKey, existingIkoListColumn.order),
        )
        return ResponseEntity.ok(ListColumnDto.from(ikoListColumn))
    }

    @RunWithoutAuthorization
    @PutMapping("/v1/iko-data-aggregate/{ikoDataAggregateKey}/column")
    fun updateIkoListColumnsOrder(
        @PathVariable ikoDataAggregateKey: String,
        @RequestBody request: List<IkoListColumnUpdateRequest>,
    ): ResponseEntity<List<ListColumnDto>> {
        val existingIkoListColumns = service.findAllColumnsByIkoDataAggregateKey(
            ikoDataAggregateKey = ikoDataAggregateKey,
        )
        require(request.map { it.key }.toSet() == existingIkoListColumns.map { it.key }.toSet())
        val ikoListColumns = request.mapIndexed { index, updatedListColumn ->
            val existingListColumn = existingIkoListColumns.first { it.key == updatedListColumn.key }
            service.update(
                ikoDataAggregateKey = ikoDataAggregateKey,
                listColumn = existingListColumn.copy(order = index),
            )
        }
        return ResponseEntity.ok(ikoListColumns.map { ListColumnDto.from(it) })
    }

    @RunWithoutAuthorization
    @DeleteMapping("/v1/iko-data-aggregate/{ikoDataAggregateKey}/column/{key}")
    fun deleteIkoListColumn(
        @PathVariable ikoDataAggregateKey: String,
        @PathVariable key: String,
    ): ResponseEntity<ListColumnDto> {
        service.deleteByKey(ikoDataAggregateKey, key)
        return ResponseEntity.noContent().build()
    }
}
