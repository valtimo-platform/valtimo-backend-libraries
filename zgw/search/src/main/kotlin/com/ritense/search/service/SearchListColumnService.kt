/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class SearchListColumnService(
    private val searchListColumnRepository: SearchListColumnRepository
) {

    fun create(searchListColumn: SearchListColumn) = searchListColumnRepository.save(searchListColumn)

    fun update(ownerId: String, key: String, searchListColumn: SearchListColumn) =
        with(findByOwnerIdAndKey(ownerId, key)) {
            if (this != null) {
                if (searchListColumn.ownerId != ownerId) {
                    throw ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This ownerId already exists. Please choose another ownerId"
                    )
                } else if (searchListColumn.key != key) {
                    throw ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This key already exists. Please choose another key"
                    )
                }
            }
            searchListColumnRepository.save(searchListColumn)
        }

    fun findByOwnerId(ownerId: String) = searchListColumnRepository.findByIdOrNull(ownerId)

    fun findByOwnerIdAndKey(ownerId: String, key: String) = searchListColumnRepository.findByOwnerIdAndKey(ownerId, key)

    fun delete(ownerId: String, key: String) =
        with(findByOwnerIdAndKey(ownerId, key)) {
            this?.let { searchListColumnRepository.delete(it) }
        }
}