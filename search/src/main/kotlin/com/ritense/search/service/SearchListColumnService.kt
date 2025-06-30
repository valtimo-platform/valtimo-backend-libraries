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

import com.ritense.search.domain.SearchListColumn
import com.ritense.search.repository.SearchListColumnRepository
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
@SkipComponentScan
class SearchListColumnService(
    private val searchListColumnRepository: SearchListColumnRepository
) {

    fun create(column: SearchListColumn): SearchListColumn {
        require(findById(column.id) == null)
        require(findByOwnerAndKey(column.ownerType, column.ownerId, column.key) == null)
        return searchListColumnRepository.save(column)
    }

    fun update(column: SearchListColumn): SearchListColumn {
        val existingColumn = findById(column.id)
            ?: findByOwnerAndKey(column.ownerType, column.ownerId, column.key)
            ?: throw IllegalStateException("Search list column not found")
        return searchListColumnRepository.save(column.copy(id = existingColumn.id))
    }

    fun findByOwner(ownerType: String, ownerId: String) =
        searchListColumnRepository.findAllByOwnerTypeAndOwnerIdOrderByOrder(ownerType, ownerId)

    fun findById(id: UUID): SearchListColumn? =
        searchListColumnRepository.findById(id).getOrNull()

    fun findByOwnerAndKey(ownerType: String, ownerId: String, key: String): SearchListColumn? =
        searchListColumnRepository.findByOwnerTypeAndOwnerIdAndKeyOrderByOrder(ownerType, ownerId, key)

    fun deleteAllByOwner(ownerType: String, ownerId: String) =
        searchListColumnRepository.deleteAllByOwnerTypeAndOwnerId(ownerType, ownerId)

    fun delete(ownerType: String, ownerId: String, key: String) =
        searchListColumnRepository.deleteAllByOwnerTypeAndOwnerIdAndKey(ownerType, ownerId, key)

    fun updateList(searchListColumn: List<SearchListColumn>) {
        searchListColumnRepository.saveAll(
            searchListColumn.mapIndexed { index, column ->
                column.copy(order = index)
            }
        )
    }

}