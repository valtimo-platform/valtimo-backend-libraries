/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.zgw

import mu.KLogger
import mu.KotlinLogging
import java.net.URI
import java.util.Collections

data class Page<T>(
    val count: Int,
    val next: URI? = null,
    val previous: URI? = null,
    val results: List<T>
) {
    inline fun <R: Comparable<*>> sortedBy(crossinline selector: (T) -> R): Page<T> {
        Collections.sort(results, compareBy(selector))
        return this
    }

    companion object {
        fun <T> getAll(
            pageLimit: Int = 100,
            getPage: (page: Int) -> Page<T>
        ): List<T> {
            require(pageLimit > 0) { "pageLimit should be > 0 but was: $pageLimit" }

            var page = 1
            val results = generateSequence(getPage(page)) { previousPage ->
                if (page < pageLimit && previousPage.next != null) {
                    getPage(++page)
                } else {
                    null
                }
            }.toList()

            if (results.last().next != null) {
                logger.error { "Too many page request: Truncated after $page pages. Please use a paginated result!" }
            } else if (page >= pageLimit / 2) {
                logger.warn { "Retrieved $page pages. Page limit is $pageLimit. Please consider using a paginated result!" }
            }

            return results.flatMap(Page<T>::results)
        }

        private val logger: KLogger = KotlinLogging.logger {}
    }
}