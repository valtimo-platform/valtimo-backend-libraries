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

package com.ritense.search.service

import com.ritense.search.domain.SearchFieldV2
import com.ritense.search.repository.SearchFieldV2Repository
import com.ritense.search.web.rest.dto.SearchFieldV2Dto
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.springframework.stereotype.Service

@Service
@SkipComponentScan
class SearchFieldV2Service(
    private val searchFieldV2Repository: SearchFieldV2Repository,
) {

    fun create(searchFieldDto: SearchFieldV2Dto): SearchFieldV2 {
        val searchField = searchFieldDto.toEntity()
        require(searchFieldV2Repository.findById(searchField.id).isEmpty)
        require(findByOwnerAndKey(searchFieldDto.ownerType, searchFieldDto.ownerId, searchFieldDto.key) == null)
        return searchFieldV2Repository.save(searchField)
    }

    fun update(searchFieldDto: SearchFieldV2Dto): SearchFieldV2? {
        val existingSearchField = searchFieldV2Repository.findById(searchFieldDto.id).orElse(null)
            ?: findByOwnerAndKey(searchFieldDto.ownerType, searchFieldDto.ownerId, searchFieldDto.key)
            ?: error("Failed to update search field. No Search field found with owner '${searchFieldDto.ownerType}:${searchFieldDto.ownerId}' and key '${searchFieldDto.key}'")
        return searchFieldV2Repository.save(searchFieldDto.toEntity().copy(id = existingSearchField.id))
    }

    fun findAllByOwnerType(ownerType: String) =
        searchFieldV2Repository.findAllByOwnerTypeOrderByOrder(ownerType)

    fun findAllByOwner(ownerType: String, ownerId: String) =
        searchFieldV2Repository.findAllByOwnerTypeAndOwnerIdOrderByOrder(ownerType, ownerId)

    fun findByOwnerAndKey(ownerType: String, ownerId: String, key: String) =
        searchFieldV2Repository.findByOwnerTypeAndOwnerIdAndKeyOrderByOrder(ownerType, ownerId, key)

    fun delete(ownerType: String, ownerId: String, key: String) =
        searchFieldV2Repository.deleteAllByOwnerTypeAndOwnerIdAndKey(ownerType, ownerId, key)

    fun deleteAllByOwner(ownerType: String, ownerId: String) =
        searchFieldV2Repository.deleteAllByOwnerTypeAndOwnerId(ownerType, ownerId)

    fun updateList(searchFieldDtos: List<SearchFieldV2Dto>): List<SearchFieldV2> {
        val searchFields = searchFieldDtos.mapIndexed { index, field -> field.toEntity().copy(order = index) }
        return searchFieldV2Repository.saveAll(searchFields)
    }

}