/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
package com.ritense.objectenapi.client

data class ObjectSearchParameter(
    val attribute: String,
    val comparator: Comparator,
    val value: String
) {
    fun toQueryParameter() = attribute + "__" + comparator.value + "__" + value

    companion object {
        fun toQueryParameter(objectSearchParameters: List<ObjectSearchParameter>): String {
            return objectSearchParameters.joinToString(separator = ",") {
                it.toQueryParameter()
            }
        }
    }
}
enum class Comparator(val value: String) {
    EQUAL_TO("exact"),
    GREATER_THAN("gt"),
    GREATER_THAN_OR_EQUAL_TO("gte"),
    LOWER_THAN("lt"),
    LOWER_THAN_OR_EQUAL_TO("lte"),
    STRING_CONTAINS("icontains"),
}