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

import com.ritense.iko.service.IkoDataRequestService
import com.ritense.iko.service.IkoListColumnService
import com.ritense.iko.service.IkoSearchFieldService
import com.ritense.iko.web.rest.request.IkoSearchRequest
import com.ritense.iko.web.rest.response.IkoDataRequestUserListResponse
import com.ritense.iko.web.rest.response.IkoSearchResponse
import com.ritense.search.domain.SearchFieldMatchType
import com.ritense.search.domain.SearchFieldV2
import com.ritense.search.domain.SearchListColumn
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
    private val listColumnService: IkoListColumnService,
    private val searchFieldService: IkoSearchFieldService,
) {

    @GetMapping("/v1/iko-data-aggregate/{ikoDataAggregateKey}/data-request")
    fun getIkoDataRequests(
        @PathVariable ikoDataAggregateKey: String,
    ): ResponseEntity<List<IkoDataRequestUserListResponse>> {
        val ikoDataRequests = dataRequestService.findAll(
            ikoDataAggregateKey = ikoDataAggregateKey,
        )
        val response = ikoDataRequests.map { ikoDataRequest ->
            val searchFields =
                searchFieldService.findAllSearchFieldsByIkoDataRequest(ikoDataAggregateKey, ikoDataRequest.id.key)
            IkoDataRequestUserListResponse.from(ikoDataRequest, searchFields)
        }
        return ResponseEntity.ok(response)
    }

    @PostMapping("/v1/iko-data-aggregate/{ikoDataAggregateKey}/data-request/{ikoDataRequestKey}/search")
    fun search(
        @PathVariable ikoDataAggregateKey: String,
        @PathVariable ikoDataRequestKey: String,
        @RequestBody request: IkoSearchRequest,
        pageable: Pageable,
    ): ResponseEntity<IkoSearchResponse> {
        val headers = listColumnService.findAllColumnsByIkoDataAggregateKey(ikoDataAggregateKey)
        val searchFields = searchFieldService.findAllSearchFieldsByIkoDataRequest(ikoDataAggregateKey, ikoDataRequestKey)
        require(searchFields.filter { it.required }.all { request.filters.containsKey(it.key) }) {
            "Missing required SearchField for DataRequest '$ikoDataAggregateKey:$ikoDataRequestKey'"
        }
        val data = dataRequestService.searchData(
            key = ikoDataRequestKey,
            ikoDataAggregateKey = ikoDataAggregateKey,
            filters = toDataFilers(request.filters, searchFields),
            pageable = toPageableByPath(headers, pageable),
        )
        return ResponseEntity.ok(IkoSearchResponse.from(headers, data))
    }

    private fun toDataFilers(filters: Map<String, Any?>, searchFields: List<SearchFieldV2>): List<DataFilter> {
        return filters.map { filter ->
            val searchField = searchFields.single { it.key == filter.key }
            DataFilter(searchField.path, searchField.matchType!!.toComparator(), filter.value)
        }
    }

    private fun toPageableByPath(headers: List<SearchListColumn>, pageable: Pageable): Pageable {
        val sortOrders = pageable.sort.map { sort ->
            Sort.Order.by(headers.single { header -> header.key == sort.property }.path)
        }
        return PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by(sortOrders.toList()))
    }

    private fun SearchFieldMatchType.toComparator(): Comparator {
        return when (this) {
            SearchFieldMatchType.EXACT -> Comparator.EQUAL_TO
            SearchFieldMatchType.LIKE -> Comparator.STRING_CONTAINS
        }
    }
}
