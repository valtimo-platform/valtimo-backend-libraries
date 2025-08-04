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

package com.ritense.zakenapi.domain

data class SearchParameter(
    val attribute: String,
    val comparator: Comparator,
    val value: String?
) {

    fun getQueryParamName(): String {
        val path = if (attribute.startsWith('/')) {
            attribute.substring(1).replace("/", "__")
        } else {
            attribute
        }

        return if (isNull() || isNotNull()) {
            path + "__" + Comparator.IS_NULL
        } else if (comparator == Comparator.EQUAL_TO) {
            path
        } else {
            path + "__" + comparator.value
        }
    }

    fun getQueryParamValue(): String {
        return if (isNull()) {
            "true"
        } else if (isNotNull()) {
            "false"
        } else {
            value.toString()
        }
    }

    private fun isNull(): Boolean {
        return value?.equals("true", ignoreCase = true) == true && comparator == Comparator.IS_NULL ||
            value == null && comparator == Comparator.EQUAL_TO
    }

    private fun isNotNull(): Boolean {
        return value?.equals("true", ignoreCase = true) == false && comparator == Comparator.IS_NULL
    }
}

enum class Comparator(val value: String) {
    EQUAL_TO(""),
    GREATER_THAN("gt"),
    GREATER_THAN_OR_EQUAL_TO("gte"),
    LOWER_THAN("lt"),
    LOWER_THAN_OR_EQUAL_TO("lte"),
    STRING_CONTAINS("icontains"),
    IN("in"),
    IS_NULL("isnull"),
}
