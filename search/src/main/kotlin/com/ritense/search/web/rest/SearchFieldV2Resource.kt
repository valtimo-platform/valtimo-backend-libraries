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

package com.ritense.search.web.rest

import com.ritense.search.domain.SearchFieldV2
import com.ritense.search.service.SearchFieldV2Service
import javax.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/api/v1/search/field", produces = [MediaType.APPLICATION_JSON_VALUE])
class SearchFieldV2Resource(
    private val searchFieldV2Service: SearchFieldV2Service
) {

    @PostMapping("/{ownerId}")
    fun create(
        @PathVariable ownerId: String,
        @Valid @RequestBody searchFieldV2: SearchFieldV2
    ) =
        ResponseEntity.ok(searchFieldV2Service.create(searchFieldV2))

    @PutMapping("/{ownerId}/{key}")
    fun update(
        @PathVariable ownerId: String,
        @PathVariable key: String,
        @Valid @RequestBody searchFieldV2: SearchFieldV2
    ) =
        ResponseEntity.ok(searchFieldV2Service.update(ownerId, key, searchFieldV2))

    @PutMapping("/{ownerId}/fields")
    fun updateList(
        @PathVariable ownerId: String,
        @Valid @RequestBody searchFieldV2: List<SearchFieldV2>
    ) =
        ResponseEntity.ok(searchFieldV2Service.updateList(ownerId, searchFieldV2))

    @GetMapping("/{ownerId}")
    fun getAllByOwnerId(@PathVariable ownerId: String) =
        ResponseEntity.ok(searchFieldV2Service.findAllByOwnerId(ownerId))

    @DeleteMapping("/{ownerId}/{key}")
    fun delete(
        @PathVariable ownerId: String,
        @PathVariable key: String
    ): ResponseEntity<Any> {
        searchFieldV2Service.delete(ownerId, key)
        return ResponseEntity.noContent().build()
    }
}