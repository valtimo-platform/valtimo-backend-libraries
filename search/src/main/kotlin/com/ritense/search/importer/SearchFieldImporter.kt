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

package com.ritense.search.importer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.SEARCH_FIELD
import com.ritense.search.deployment.ReadFileSearchFieldDto
import com.ritense.search.repository.SearchFieldV2Repository
import com.ritense.search.service.SearchFieldV2Service

abstract class SearchFieldImporter(
    private val objectMapper: ObjectMapper,
    private val repository: SearchFieldV2Repository,
    private val searchFieldService: SearchFieldV2Service,
    private val ownerTypeKey: String,
) : Importer {
    override fun type(): String = SEARCH_FIELD

    protected fun deploy(ownerId: String, searchFields: List<ReadFileSearchFieldDto>) {
        repository.deleteAllByOwnerTypeAndOwnerId(ownerTypeKey, ownerId)

        searchFields.mapIndexed { index, searchField ->
            val mappedField = searchField.toSearchFieldDto(ownerId, ownerTypeKey, index)
            repository.findByOwnerTypeAndOwnerIdAndKeyOrderByOrder(ownerTypeKey, ownerId, mappedField.key)
                ?.let { _ ->
                    searchFieldService.update(ownerId, searchField.key, mappedField)
                } ?: searchFieldService.create(mappedField)
        }
    }
}