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

import com.ritense.search.domain.LEGACY_OWNER_TYPE
import com.ritense.search.domain.SearchFieldV2
import com.ritense.search.mapper.SearchFieldV2Mapper
import com.ritense.search.repository.SearchFieldV2Repository
import com.ritense.search.web.rest.dto.SearchFieldV2Dto
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
@SkipComponentScan
class SearchFieldV2Service(
    private val searchFieldV2Repository: SearchFieldV2Repository,
    private val searchFieldMappers: List<SearchFieldV2Mapper>
) {

    fun create(searchFieldV2Dto: SearchFieldV2Dto): SearchFieldV2 {
        val searchFieldV2 = getSearchFieldMapper(searchFieldV2Dto.ownerType).toNewSearchFieldV2(searchFieldV2Dto)

        return searchFieldV2Repository.save(searchFieldV2)
    }

    fun update(ownerId: String, key: String, searchFieldV2Dto: SearchFieldV2Dto): SearchFieldV2? {
        val searchFieldV2 = getSearchFieldMapper(searchFieldV2Dto.ownerType).toNewSearchFieldV2(searchFieldV2Dto)

        if (searchFieldV2.ownerId != ownerId) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Failed to update search field. Mismatching ownerId's"
            )
        } else if (searchFieldV2.key != key) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Failed to update search field. Mismatching key's"
            )
        }

        val existingSearchFieldV2 = findByOwnerTypeAndOwnerIdAndKey(searchFieldV2Dto.ownerType, ownerId, key)
            ?: throw IllegalStateException("Failed to update search field. No Search field found with ownerId '$ownerId' and key '$key'")

        return searchFieldV2Repository.save(
            existingSearchFieldV2.copy(
                ownerId = searchFieldV2.ownerId,
                key = searchFieldV2.key,
                path = searchFieldV2.path,
                title = searchFieldV2.title,
                order = searchFieldV2.order,
                dataType = searchFieldV2.dataType,
                fieldType = searchFieldV2.fieldType,
                matchType = searchFieldV2.matchType
            )
        )
    }

    fun findAllByOwnerType(ownerId: String) = searchFieldV2Repository.findAllByOwnerTypeOrderByOrder(ownerId)

    @Deprecated("Since 12.1.0", ReplaceWith("com.ritense.search.service.SearchFieldV2Service.findAllByOwnerTypeAndOwnerId()"))
    fun findAllByOwnerId(ownerId: String) = searchFieldV2Repository.findAllByOwnerTypeAndOwnerIdOrderByOrder(LEGACY_OWNER_TYPE, ownerId)

    fun findAllByOwnerTypeAndOwnerId(ownerType: String, ownerId: String) = searchFieldV2Repository.findAllByOwnerTypeAndOwnerIdOrderByOrder(ownerType, ownerId)

    @Deprecated("Since 12.1.0", ReplaceWith("com.ritense.search.service.SearchFieldV2Service.findByOwnerTypeAndOwnerIdAndKey()"))
    fun findByOwnerIdAndKey(ownerId: String, key: String) = searchFieldV2Repository.findByOwnerTypeAndOwnerIdAndKeyOrderByOrder(LEGACY_OWNER_TYPE, ownerId, key)

    fun findByOwnerTypeAndOwnerIdAndKey(ownerType: String, ownerId: String, key: String) = searchFieldV2Repository.findByOwnerTypeAndOwnerIdAndKeyOrderByOrder(ownerType, ownerId, key)

    @Deprecated("Since 12.1.0", ReplaceWith("com.ritense.search.service.SearchFieldV2Service.delete()"))
    fun delete(ownerId: String, key: String) =
        with(findByOwnerIdAndKey(ownerId, key)) {
            this?.let { searchFieldV2Repository.delete(it) }
        }

    fun delete(ownerType: String, ownerId: String, key: String) =
        with(findByOwnerTypeAndOwnerIdAndKey(ownerType, ownerId, key)) {
            this?.let { searchFieldV2Repository.delete(it) }
        }

    fun updateList(ownerId: String, searchFieldV2Dtos: List<SearchFieldV2Dto>): List<SearchFieldV2> {
        val searchFieldV2 = searchFieldV2Dtos.map { getSearchFieldMapper(it.ownerType).toNewSearchFieldV2(it) }

        return searchFieldV2Repository.saveAll(
            searchFieldV2.mapIndexed{
                index, field ->  field.copy(order = index)
            }
        )
    }

    private fun getSearchFieldMapper(ownerType: String): SearchFieldV2Mapper {
        return searchFieldMappers.singleOrNull { it.supportsOwnerType(ownerType) }
            ?: throw IllegalStateException("No ProcessLinkMapper found for processLinkType $ownerType")
    }

}