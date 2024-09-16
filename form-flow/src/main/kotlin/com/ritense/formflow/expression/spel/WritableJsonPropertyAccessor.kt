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

package com.ritense.formflow.expression.spel

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.formflow.json.MapperSingleton
import org.springframework.expression.EvaluationContext
import org.springframework.integration.json.JsonPropertyAccessor
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.isAccessible

class WritableJsonPropertyAccessor : JsonPropertyAccessor() {


    override fun canWrite(context: EvaluationContext, target: Any?, name: String): Boolean {
        return isJsonNode(target)
    }

    override fun write(context: EvaluationContext, target: Any?, name: String, newValue: Any?) {
        requireNotNull(target)
        val targetNode = getJsonNode(target)
        val newValueNode = toJsonNode(newValue)

        when (targetNode) {
            is ObjectNode -> targetNode.set<JsonNode>(name, newValueNode)
            is ArrayNode -> targetNode.add(newValueNode)
            else -> throw UnsupportedOperationException("Write to '${targetNode.javaClass}' is not supported")
        }
    }

    private fun toJsonNode(value: Any?): JsonNode {
        return if (value is Collection<*> && value.isEmpty()) {
            MapperSingleton.get().createObjectNode()
        } else {
            MapperSingleton.get().valueToTree(value)
        }
    }

    private fun isJsonNode(target: Any?): Boolean {
        return target != null && (target is JsonNode ||
            target.javaClass.name == "org.springframework.integration.json.JsonPropertyAccessor\$ComparableJsonNode")
    }

    private fun getJsonNode(target: Any): JsonNode {
        return if (target is JsonNode) {
            target
        } else {
            val getRealNodeFunction = JsonPropertyAccessor::class
                .nestedClasses.single { it.simpleName == "ComparableJsonNode" }
                .declaredFunctions.single { it.name == "getRealNode" }
            getRealNodeFunction.isAccessible = true
            getRealNodeFunction.call(target) as JsonNode
        }
    }
}
