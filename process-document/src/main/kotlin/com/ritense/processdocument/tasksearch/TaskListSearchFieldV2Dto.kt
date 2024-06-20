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
