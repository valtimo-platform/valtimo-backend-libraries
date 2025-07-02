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

package com.ritense.search.web.rest.dto

import com.ritense.search.domain.DataType
import com.ritense.search.domain.FieldType
import com.ritense.search.domain.LEGACY_OWNER_TYPE
import com.ritense.search.domain.SearchFieldMatchType
import com.ritense.search.domain.SearchFieldV2
import java.util.UUID

data class SearchFieldV2Dto(
    val id: UUID = UUID.randomUUID(),
    val ownerId: String,
    val ownerType: String = LEGACY_OWNER_TYPE,
    val key: String,
    val title: String?,
    val path: String,
    val order: Int,
    val dataType: DataType,
    val fieldType: FieldType,
    val matchType: SearchFieldMatchType? = null,
    val dropdownDataProvider: String? = null,
    val required: Boolean = false,
) {
    fun toEntity() = SearchFieldV2(
        id = this.id,
        ownerId = this.ownerId,
        ownerType = this.ownerType,
        key = this.key,
        title = this.title,
        path = this.path,
        order = this.order,
        dataType = this.dataType,
        fieldType = this.fieldType,
        matchType = this.matchType,
        dropdownDataProvider = this.dropdownDataProvider,
        required = this.required,
    )

    companion object {
        fun of(entity: SearchFieldV2): SearchFieldV2Dto {
            return SearchFieldV2Dto(
                id = entity.id,
                ownerId = entity.ownerId,
                ownerType = entity.ownerType,
                key = entity.key,
                title = entity.title,
                path = entity.path,
                order = entity.order,
                dataType = entity.dataType,
                fieldType = entity.fieldType,
                matchType = entity.matchType,
                dropdownDataProvider = entity.dropdownDataProvider,
                required = entity.required,
            )
        }
    }
}
