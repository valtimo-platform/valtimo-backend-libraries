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

import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.SEARCH_FIELD
import com.ritense.search.deployment.ReadFileSearchFieldDto
import com.ritense.search.service.SearchFieldV2Service

abstract class SearchFieldImporter(
    private val searchFieldService: SearchFieldV2Service,
    private val ownerType: String,
) : Importer {
    override fun type(): String = SEARCH_FIELD

    override fun dependsOn(): Set<String> = emptySet()

    protected fun deploy(ownerId: String, searchFields: List<ReadFileSearchFieldDto>) {
        searchFieldService.deleteAllByOwner(ownerType, ownerId)

        searchFields.mapIndexed { index, searchField ->
            val mappedField = searchField.toSearchFieldDto(ownerId, ownerType, index)
            if (searchFieldService.findByOwnerAndKey(ownerType, ownerId, mappedField.key) != null) {
                searchFieldService.update(mappedField)
            } else {
                searchFieldService.create(mappedField)
            }
        }
    }
}