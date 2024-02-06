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

package com.ritense.search.web.rest

import com.ritense.search.domain.SearchListColumn
import com.ritense.search.service.SearchListColumnService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import jakarta.validation.Valid
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
@RequestMapping("/api/v1/search/list-column", produces = [APPLICATION_JSON_UTF8_VALUE])
class SearchListColumnResource(
    private val searchListColumnService: SearchListColumnService
) {

    @PostMapping("/{ownerId}")
    fun create(
        @PathVariable ownerId: String,
        @Valid @RequestBody searchListColumn: SearchListColumn
    ) =
        ResponseEntity.ok(searchListColumnService.create(searchListColumn))

    @PutMapping("/{ownerId}/{key}")
    fun update(
        @PathVariable ownerId: String,
        @PathVariable key: String,
        @Valid @RequestBody searchListColumn: SearchListColumn
    ) =
        ResponseEntity.ok(searchListColumnService.update(ownerId, key, searchListColumn))

    @PutMapping("/{ownerId}/search-list-columns")
    fun updateList(
        @PathVariable ownerId: String,
        @Valid @RequestBody searchListColumn: List<SearchListColumn>
    ) =
        ResponseEntity.ok(searchListColumnService.updateList(ownerId, searchListColumn))

    @GetMapping("/{ownerId}")
    fun getByKey(@PathVariable ownerId: String) =
        ResponseEntity.ok(searchListColumnService.findByOwnerId(ownerId))

    @DeleteMapping("/{ownerId}/{key}")
    fun delete(
        @PathVariable ownerId: String,
        @PathVariable key: String
    ): ResponseEntity<Any> {
        searchListColumnService.delete(ownerId, key)
        return ResponseEntity.noContent().build()
    }
}
