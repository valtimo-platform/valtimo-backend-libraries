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

package com.ritense.widget

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort

class PageWithData<T>(
    val content: List<T>,
    val first: Boolean,
    val last: Boolean,
    val totalPages: Int,
    val totalElements: Long,
    val numberOfElements: Int,
    val size: Int,
    val number: Int,
    val sort: Sort,
    @JsonAnyGetter
    private val data: Map<String, Any> = emptyMap(),
) {

    constructor(page: Page<T>, vararg data: Any) : this(
        content = page.content,
        first = page.isFirst,
        last = page.isLast,
        totalPages = page.totalPages,
        totalElements = page.totalElements,
        numberOfElements = page.numberOfElements,
        size = page.size,
        number = page.number,
        sort = page.sort,
        data = mergeData(data.toList())
    )

    @JsonAnyGetter
    fun any(): Map<String, Any> = data

    companion object {
        private fun mergeData(data: List<Any>): Map<String, Any> {
            return data
                .map { item -> jacksonObjectMapper().convertValue<Map<String, Any>>(item) }
                .flatMap { it.entries }
                .associate { it.key to it.value }
        }
    }
}
