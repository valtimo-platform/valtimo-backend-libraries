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
import com.ritense.search.service.SearchListColumnService

abstract class SearchListColumnImporter(
    private val service: SearchListColumnService,
    private val ownerType: String,
) : Importer {

    protected fun deploy(ownerId: String, listColumns: List<SearchListColumnDto>) {
        service.deleteAllByOwner(ownerType, ownerId)

        listColumns.mapIndexed { index, listColumn ->
            val mappedField = listColumn.toEntity(ownerId, ownerType, index)
            if ((listColumn.id != null && service.findById(listColumn.id) != null) ||
                service.findByOwnerAndKey(ownerType, ownerId, mappedField.key) != null) {
                service.update(mappedField)
            } else {
                service.create(mappedField)
            }
        }
    }
}