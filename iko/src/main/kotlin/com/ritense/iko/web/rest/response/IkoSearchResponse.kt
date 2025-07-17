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

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.search.domain.SearchListColumn
import org.springframework.data.domain.Page

data class IkoSearchResponse(
    val headers: List<IkoListColumnResponse>,
    val rows: Page<IkoRowDto>,
) {
    companion object {
        fun from(headers: List<SearchListColumn>, rows: Page<JsonNode>) = IkoSearchResponse(
            headers = headers.map { IkoListColumnResponse.from(it) },
            rows = rows.map { rowData -> IkoRowDto(headers.map { header -> IkoItemDto.from(header, rowData) }) },
        )
    }

    data class IkoRowDto(
        val items: List<IkoItemDto>,
    )

    data class IkoItemDto(
        val key: String,
        val value: Any?,
    ) {
        companion object {
            fun from(header: SearchListColumn, rowData: JsonNode) = IkoItemDto(
                key = header.key,
                value = rowData.at(header.path.substringAfterLast(':'))
            )
        }
    }
}