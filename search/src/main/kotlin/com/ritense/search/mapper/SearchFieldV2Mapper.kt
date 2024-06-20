package com.ritense.search.mapper

import com.ritense.search.domain.SearchFieldV2
import com.ritense.search.web.rest.dto.SearchFieldV2Dto


interface SearchFieldV2Mapper {
    fun supportsOwnerType(ownerType: String): Boolean
    fun toNewSearchFieldV2(searchFieldV2Dto: SearchFieldV2Dto): SearchFieldV2
}