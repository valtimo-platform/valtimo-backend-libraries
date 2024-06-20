package com.ritense.search.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.search.domain.LEGACY_OWNER_TYPE
import com.ritense.search.domain.SearchFieldV2
import com.ritense.search.web.rest.dto.LegacySearchFieldV2Dto
import com.ritense.search.web.rest.dto.SearchFieldV2Dto


class LegacySearchFieldV2Mapper(
    objectMapper: ObjectMapper
): SearchFieldV2Mapper {

    init{
        objectMapper.registerSubtypes(
            LegacySearchFieldV2Dto::class.java
        )
    }

    override fun supportsOwnerType(ownerType: String) = ownerType == LEGACY_OWNER_TYPE

    override fun toNewSearchFieldV2(searchFieldV2Dto: SearchFieldV2Dto): SearchFieldV2 {
        return SearchFieldV2(
            id = searchFieldV2Dto.id,
            ownerId = searchFieldV2Dto.ownerId,
            ownerType = LEGACY_OWNER_TYPE,
            key = searchFieldV2Dto.key,
            title = searchFieldV2Dto.title,
            path = searchFieldV2Dto.path,
            order = searchFieldV2Dto.order,
            dataType = searchFieldV2Dto.dataType,
            fieldType = searchFieldV2Dto.fieldType,
            matchType = searchFieldV2Dto.matchType,
            dropdownDataProvider = searchFieldV2Dto.dropdownDataProvider
        )
    }

}