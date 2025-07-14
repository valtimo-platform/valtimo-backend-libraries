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

import com.ritense.iko.domain.IkoDataRequest
import com.ritense.iko.importer.IkoSearchFieldDto
import com.ritense.search.domain.SearchFieldV2

data class IkoDataRequestUserListResponse(
    val key: String,
    val title: String,
    val searchFields: List<IkoSearchFieldDto>,
) {
    companion object {
        fun from(entity: IkoDataRequest, searchFields: List<SearchFieldV2>) = IkoDataRequestUserListResponse(
            key = entity.id.key,
            title = entity.title,
            searchFields = searchFields.map { IkoSearchFieldDto.of(it) }
        )
    }
}