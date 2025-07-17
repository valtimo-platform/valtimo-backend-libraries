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

package com.ritense.iko.web.rest.response

import com.ritense.search.domain.DataType
import com.ritense.search.domain.FieldType
import com.ritense.search.domain.SearchFieldMatchType
import com.ritense.search.domain.SearchFieldV2

data class IkoSearchFieldResponse(
    val key: String,
    val title: String?,
    val path: String,
    val order: Int,
    val dataType: DataType,
    val fieldType: FieldType,
    val matchType: SearchFieldMatchType?,
    val dropdownDataProvider: String?,
    val required: Boolean,
) {
    companion object {
        fun from(entity: SearchFieldV2) = IkoSearchFieldResponse(
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