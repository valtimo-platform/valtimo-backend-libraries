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

package com.ritense.search.service

import com.ritense.search.domain.SearchFieldV2
import com.ritense.search.repository.SearchFieldV2Repository
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class SearchFieldV2Service(
    private val searchFieldV2Repository: SearchFieldV2Repository
) {

    fun create(searchFieldV2: SearchFieldV2) = searchFieldV2Repository.save(searchFieldV2)

    fun update(ownerId: String, key: String, searchFieldV2: SearchFieldV2) =
        with(findByOwnerIdAndKey(ownerId, key)) {
            if (this != null) {
                if (searchFieldV2.ownerId != ownerId) {
                    throw ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This ownerId already exists. Please choose another ownerId"
                    )
                } else if (searchFieldV2.key != key) {
                    throw ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This key already exists. Please choose another key"
                    )
                }
            }
            searchFieldV2Repository.save(this?.copy(
                ownerId = searchFieldV2.ownerId,
                key = searchFieldV2.key,
                path = searchFieldV2.path,
                title = searchFieldV2.title,
                order = searchFieldV2.order,
                dataType = searchFieldV2.dataType,
                fieldType = searchFieldV2.fieldType
            ))
        }

    fun findAllByOwnerId(ownerId: String) = searchFieldV2Repository.findAllByOwnerId(ownerId)

    fun findByOwnerIdAndKey(ownerId: String, key: String) = searchFieldV2Repository.findByOwnerIdAndKey(ownerId, key)

    fun delete(ownerId: String, key: String) =
        with(findByOwnerIdAndKey(ownerId, key)) {
            this?.let { searchFieldV2Repository.delete(it) }
        }

}