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

package com.ritense.search.autodeployment

import com.ritense.search.domain.ColumnDefaultSort
import com.ritense.search.domain.DisplayType
import com.ritense.search.domain.LEGACY_OWNER_TYPE
import com.ritense.search.domain.SearchListColumn
import java.util.UUID

data class SearchListColumnDto(
    val id: UUID = UUID.randomUUID(),
    val ownerId: String,
    val ownerType: String?,
    val key: String,
    val title: String?,
    val path: String,
    val order: Int,
    val displayType: DisplayType,
    val sortable: Boolean,
    val defaultSort: ColumnDefaultSort? = null,
) {
    fun toEntity() = SearchListColumn(
        id = this.id,
        ownerId = this.ownerId,
        ownerType = this.ownerType ?: LEGACY_OWNER_TYPE,
        key = this.key,
        title = this.title,
        path = this.path,
        order = this.order,
        displayType = this.displayType,
        sortable = this.sortable,
        defaultSort = this.defaultSort,
    )
}