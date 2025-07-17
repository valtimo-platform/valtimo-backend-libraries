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

package com.ritense.iko.web.rest.request

import com.ritense.search.domain.DataType
import com.ritense.search.domain.FieldType
import com.ritense.search.domain.SearchFieldMatchType
import com.ritense.search.domain.SearchFieldV2
import java.util.UUID

data class IkoSearchFieldUpdateRequest(
    val key: String,
    val title: String?,
    val path: String,
    val dataType: DataType,
    val fieldType: FieldType,
    val matchType: SearchFieldMatchType?,
    val dropdownDataProvider: String?,
    val required: Boolean,
) {
    fun toEntity(id: UUID, ikoDataAggregateKey: String, ikoDataRequestKey: String, order: Int) = SearchFieldV2(
        id = id,
        ownerType = "ikoDataRequestKey",
        ownerId = "$ikoDataAggregateKey:$ikoDataRequestKey",
        key = key,
        title = title,
        path = path,
        order = order,
        dataType = dataType,
        fieldType = fieldType,
        matchType = matchType,
        dropdownDataProvider = dropdownDataProvider,
        required = required,
    )
}