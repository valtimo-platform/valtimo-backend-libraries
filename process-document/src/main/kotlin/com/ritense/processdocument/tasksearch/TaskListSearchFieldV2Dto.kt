/*
 *  Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.processdocument.tasksearch

import com.fasterxml.jackson.annotation.JsonTypeName
import com.ritense.processdocument.service.SEARCH_FIELD_OWNER_TYPE
import com.ritense.search.domain.DataType
import com.ritense.search.domain.FieldType
import com.ritense.search.domain.SearchFieldMatchType
import com.ritense.search.web.rest.dto.SearchFieldV2Dto
import java.util.UUID

@JsonTypeName(SEARCH_FIELD_OWNER_TYPE)
data class TaskListSearchFieldV2Dto(
    override val id: UUID = UUID.randomUUID(),
    override val ownerId: String,
    override val key: String,
    override val title: String?,
    override val path: String,
    override val order: Int,
    override val dataType: DataType,
    override val fieldType: FieldType,
    override val matchType: SearchFieldMatchType? = null,
    override val dropdownDataProvider: String? = null
): SearchFieldV2Dto {
    override val ownerType: String
        get() = SEARCH_FIELD_OWNER_TYPE
}
