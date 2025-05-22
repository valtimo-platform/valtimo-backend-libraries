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

package com.ritense.document.domain

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

fun JsonNode.getJsonPointers(): List<JsonPointer> {
    return getJsonPointers("", this).map { JsonPointer.valueOf(it) }
}

private fun getJsonPointers(parent: String, data: JsonNode): List<String> {
    return when (data) {
        is ObjectNode -> getJsonPointersFromObject(parent, data)
        is ArrayNode -> getJsonPointersFromArray(parent, data)
        else -> listOf(parent)
    }
}

private fun getJsonPointersFromObject(parent: String, json: ObjectNode): List<String> {
    return json.properties().flatMap { entry ->
        val childKey = "$parent/${entry.key}"
        getJsonPointers(childKey, entry.value)
    }
}

private fun getJsonPointersFromArray(parent: String, json: ArrayNode): List<String> {
    return json.flatMapIndexed { i, data ->
        getJsonPointers("$parent/$i", data)
    }
}
