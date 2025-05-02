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

package com.ritense.valueresolver

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.flipkart.zjsonpatch.JsonPatch
import com.ritense.valtimo.contract.json.patch.JsonPatchBuilder
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.delegate.VariableScope
import java.util.function.Function

/**
 * This resolver can resolve requestedValues against the variables of a process or task.
 *
 * The value of the requestedValue should be in the format pv:someProperty
 */
class ProcessVariableValueResolverFactory(
    private val runtimeService: RuntimeService,
    private val historicService: HistoryService,
    private val objectMapper: ObjectMapper,
) : ValueResolverFactory {

    override fun supportedPrefix(): String {
        return PREFIX
    }

    override fun createResolver(
        processInstanceId: String,
        variableScope: VariableScope
    ): Function<String, Any?> {
        var variablesJson: JsonNode? = null
        return Function { requestedValue ->
            val value = variableScope.getVariable(requestedValue)
            if (value != null) {
                return@Function value
            }
            if (!isPath(requestedValue)) {
                return@Function null
            }
            if (variablesJson == null) {
                variablesJson = objectMapper.valueToTree(variableScope.variables)
            }
            return@Function getValue(variablesJson!!.at(toJsonPointer(requestedValue)))
        }
    }

    override fun createResolver(documentId: String): Function<String, Any?> {
        val processInstanceIds = historicService.createHistoricProcessInstanceQuery()
            .processInstanceBusinessKey(documentId)
            .list()
            .map { it.id }
            .toTypedArray()

        return Function { requestedValue ->
            val jsonPointer = toJsonPointer(requestedValue)
            val values = historicService.createHistoricVariableInstanceQuery()
                .processInstanceIdIn(*processInstanceIds)
                .variableName(jsonPointer.matchingProperty)
                .list()
                .map { getValue(objectMapper.valueToTree<JsonNode>(it.value).at(jsonPointer.tail())) }
                .distinct()
            if (values.size > 1) {
                throw RuntimeException(
                    "Cannot infer a unique process variable value for key `$requestedValue` using the document id as businessKey. " +
                        "Please provide a variable scope, use a unique key, or use a different value resolver."
                )
            }
            values.singleOrNull()
        }
    }

    override fun handleValues(
        processInstanceId: String,
        variableScope: VariableScope?,
        values: Map<String, Any?>
    ) {
        val variableNames = values.keys
            .map { variablePath -> toJsonPointer(variablePath).matchingProperty }
            .distinct()
        val existingValues = if (variableScope != null) {
            variableNames.associateWith { variableName -> variableScope.getVariable(variableName) }
        } else {
            runtimeService.getVariables(processInstanceId, variableNames)
        }

        val root = objectMapper.valueToTree<JsonNode>(existingValues)
        buildJsonPatch(root, values)
        val newValues = objectMapper.treeToValue<Map<String, Any?>>(root)

        runtimeService.setVariables(processInstanceId, newValues)
    }

    override fun preProcessValuesForNewCase(values: Map<String, Any?>): Map<String, Any> {
        val jsonNode = objectMapper.createObjectNode()
        buildJsonPatch(jsonNode, values)
        return objectMapper.treeToValue(jsonNode)
    }

    private fun buildJsonPatch(jsonNode: JsonNode, values: Map<String, Any?>) {
        values.forEach {
            val jsonPointer = toJsonPointer(it.key.substringAfter(":"))
            val valueNode = objectMapper.valueToTree<JsonNode>(it.value)
            val jsonPatchBuilder = JsonPatchBuilder()
            jsonPatchBuilder.addJsonNodeValue(jsonNode, jsonPointer, valueNode)
            JsonPatch.applyInPlace(jsonPatchBuilder.build().toJson(), jsonNode)
        }
    }

    private fun getValue(valueNode: JsonNode): Any? {
        return if (valueNode.isMissingNode) {
            null
        } else {
            objectMapper.treeToValue(valueNode)
        }
    }

    private fun toJsonPointer(path: String): JsonPointer {
        var newPath: String = path
        if (!path.startsWith('/')) {
            newPath = "/${path}"
        }
        return JsonPointer.valueOf(newPath.replace('.', '/'))
    }

    private fun isPath(path: String): Boolean {
        return path.contains('.') || path.contains('/')
    }

    companion object {
        const val PREFIX = "pv"
    }
}
