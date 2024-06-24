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

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.processdocument.service.SEARCH_FIELD_OWNER_TYPE
import com.ritense.search.domain.SearchFieldV2
import com.ritense.search.mapper.SearchFieldV2Mapper
import com.ritense.search.web.rest.dto.SearchFieldV2Dto

class TaskListSearchFieldV2Mapper(
    objectMapper: ObjectMapper
): SearchFieldV2Mapper {

    init{
        objectMapper.registerSubtypes(
            TaskListSearchFieldV2Dto::class.java
        )
    }

    override fun supportsOwnerType(ownerType: String) = ownerType == SEARCH_FIELD_OWNER_TYPE

    override fun toNewSearchFieldV2(searchFieldV2Dto: SearchFieldV2Dto): SearchFieldV2 =
        SearchFieldV2(
            id = searchFieldV2Dto.id,
            ownerId = searchFieldV2Dto.ownerId,
            ownerType = SEARCH_FIELD_OWNER_TYPE,
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