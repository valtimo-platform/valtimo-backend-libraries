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

package com.ritense.valtimo.contract.iko

data class PropertyField(
    val key: String,
    val type: String = PROPERTY_FIELD_TYPE_TEXT,
    val tooltip: String? = null,
    val title: String = toReadableText(key),
    val dropdownList: List<Pair<String, String>>? = null,
) {

    companion object {
        const val PROPERTY_FIELD_TYPE_TEXT = "text"
        const val PROPERTY_FIELD_TYPE_INTEGER = "integer"
        const val PROPERTY_FIELD_TYPE_DROPDOWN = "dropdown"
        const val PROPERTY_FIELD_TYPE_URL = "url"
        const val PROPERTY_FIELD_TYPE_SECRET = "secret"

        fun toReadableText(input: String): String {
            val cleaned = input.replace("[^a-zA-Z0-9]+".toRegex(), " ").trim()
            val spaced = cleaned
                .replace(Regex("(?<=[a-z])(?=[A-Z])"), " ")
                .replace(Regex("(?<=[A-Z])(?=[A-Z][a-z])"), " ")
                .replace(Regex("(?<=[A-Za-z])(?=\\d)"), " ")
                .replace(Regex("(?<=\\d)(?=[A-Za-z])"), " ")
            return spaced
                .split(" ")
                .joinToString(" ") { word ->
                    if (word.matches(Regex("[A-Z]{2,}"))) word
                    else word.lowercase().replaceFirstChar(Char::uppercaseChar)
                }
        }
    }
}
