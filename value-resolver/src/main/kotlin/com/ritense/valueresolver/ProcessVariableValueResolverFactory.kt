/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

import java.util.function.Function
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.VariableScope

/**
 * This resolver can resolve requestedValues against the variables of a process or task.
 *
 * The value of the requestedValue should be in the format pv:someProperty
 */
class ProcessVariableValueResolverFactory(
    private val runtimeService: RuntimeService
) : ValueResolverFactory {

    override fun supportedPrefix(): String {
        return PREFIX
    }

    override fun createResolver(
        processInstanceId: String,
        variableScope: VariableScope
    ): Function<String, Any?> {

        return Function { requestedValue ->
            variableScope.variables[requestedValue]
        }
    }

    override fun createResolver(documentId: String): Function<String, Any?> {
        val processInstanceIds = runtimeService.createProcessInstanceQuery()
            .processInstanceBusinessKey(documentId)
            .list()
            .map { it.id }
            .toTypedArray()

        return Function { requestedValue ->
            val values = runtimeService.createVariableInstanceQuery()
                .processInstanceIdIn(*processInstanceIds)
                .variableName(requestedValue)
                .list()
                .map { it.value }
                .distinct()
            if (values.size > 1) {
                throw RuntimeException("Cannot infer a unique process variable value for key `$requestedValue` using the document id as businessKey. " +
                    "Please provide a variable scope, use a unique key, or use a different value resolver.")
            }
            values.singleOrNull()
        }
    }

    override fun handleValues(
        processInstanceId: String,
        variableScope: VariableScope?,
        values: Map<String, Any>
    ) {
        runtimeService.setVariables(processInstanceId, values)
    }

    companion object {
        const val PREFIX = "pv"
    }
}
