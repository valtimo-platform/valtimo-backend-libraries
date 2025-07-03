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

import com.ritense.iko.importer.IkoListColumnImporter.Companion.IKO_LIST_OWNER
import com.ritense.iko.importer.IkoSearchFieldImporter.Companion.IKO_SEARCH_OWNER
import com.ritense.iko.service.IkoDataRequestService
import com.ritense.iko.web.rest.request.IkoSearchRequest
import com.ritense.iko.web.rest.response.IkoDataRequestUserListResponse
import com.ritense.iko.web.rest.response.IkoSearchResponse
import com.ritense.search.domain.SearchFieldMatchType
import com.ritense.search.service.SearchFieldV2Service
import com.ritense.search.service.SearchListColumnService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valtimo.contract.iko.Comparator
import com.ritense.valtimo.contract.iko.DataFilter
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@SkipComponentScan
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class IkoDataRequestResource(
    private val dataRequestService: IkoDataRequestService,
    private val listColumnService: SearchListColumnService,
    private val searchFieldService: SearchFieldV2Service,
) {

    @GetMapping("/v1/iko-data-aggregate/{ikoDataAggregateKey}/data-request")
    fun getIkoDataRequests(
        @PathVariable ikoDataAggregateKey: String,
    ): ResponseEntity<List<IkoDataRequestUserListResponse>> {
        val ikoDataRequests = dataRequestService.findAll(
            ikoDataAggregateKey = ikoDataAggregateKey,
        )
        val response = ikoDataRequests.map {
            val searchFields = searchFieldService.findAllByOwner(IKO_SEARCH_OWNER, "$ikoDataAggregateKey:${it.id.key}")
            IkoDataRequestUserListResponse.from(it, searchFields)
        }
        return ResponseEntity.ok(response)
    }

    @PostMapping("/v1/iko-data-aggregate/{ikoDataAggregateKey}/data-request/{key}/search")
    fun search(
        @PathVariable ikoDataAggregateKey: String,
        @PathVariable key: String,
        @RequestBody request: IkoSearchRequest,
        pageable: Pageable,
    ): ResponseEntity<IkoSearchResponse> {
        val headers = listColumnService.findAllByOwner(IKO_LIST_OWNER, ikoDataAggregateKey)
        val searchFields = searchFieldService.findAllByOwner(IKO_SEARCH_OWNER, "$ikoDataAggregateKey:$key")
        require(searchFields.filter { it.required }.all { request.filters.containsKey(it.key) }) {
            "Missing required SearchField for DataRequest '$ikoDataAggregateKey:$key'"
        }
        val dataFilters = request.filters.map { filter ->
            val searchField = searchFields.firstOrNull { it.key == filter.key }
                ?: error("DataRequest '$ikoDataAggregateKey:$key' does not have SearchField: '${filter.key}'")
            DataFilter(searchField.path, searchField.matchType!!.toComparator(), filter.value)
        }
        val pathSort = Sort.by(pageable.sort.map { Sort.Order.by(it.property) })
        val pathPageable = PageRequest.of(pageable.pageNumber, pageable.pageSize, pathSort)
        val data = dataRequestService.search(
            key = key,
            ikoDataAggregateKey = ikoDataAggregateKey,
            filters = dataFilters,
            pageable = pathPageable,
        )
        return ResponseEntity.ok(IkoSearchResponse.from(headers, data))
    }

    private fun SearchFieldMatchType.toComparator(): Comparator {
        return when (this) {
            SearchFieldMatchType.EXACT -> Comparator.EQUAL_TO
            SearchFieldMatchType.LIKE -> Comparator.STRING_CONTAINS
        }
    }
}
