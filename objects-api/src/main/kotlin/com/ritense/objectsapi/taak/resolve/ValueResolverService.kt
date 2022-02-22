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

package com.ritense.objectsapi.taak.resolve

import com.ritense.processdocument.domain.ProcessInstanceId
import org.camunda.bpm.engine.delegate.VariableScope

class ValueResolverService(
    valueResolverFactories: List<ValueResolverFactory>
) {
    private val resolverFactoryMap: Map<String, ValueResolverFactory> = valueResolverFactories.groupBy { it.supportedPrefix() }
        .filter { (key, value) ->
            if(value.size == 1) true else throw RuntimeException("Found more than 1 resolver for prefix '$key': ${value.joinToString { resolver -> resolver.javaClass.simpleName }}")
        }.map { (key, value) ->
            key to value.first()
        }.toMap()


    /**
     * This method provides a way of resolving requestedValues into values using defined resolvers.
     * requestedValues are typically prefixed, like 'pv:propertyName'.
     * If not, a resolver should be configured to handle '' prefixes.
     *
     * A requestedValue can only be resolved when a resolver for that prefix is configured.
     * An unresolved requestedValue will not be included in the returned map.
     *
     * @param processInstanceId The Camunda processInstanceId these values belong to
     * @param variableScope An implementation of VariableScope. For instance: a TaskDelegate or DelegateExecution
     * @param requestedValues The requestedValues that should be resolved into values.
     * @return A map where the key is the requestedValue, and the value the resolved value.
     */
    fun resolveValues(
        processInstanceId: ProcessInstanceId,
        variableScope: VariableScope,
        requestedValues: List<String>
    ): Map<String, Any> {
        //Group by prefix
        return requestedValues.groupBy {
            it.substringBefore(":", missingDelimiterValue = "")
        }.mapNotNull { (prefix, requestedValues) ->
            //Create a resolver per prefix group
            val resolverFactory = resolverFactoryMap[prefix]?:throw RuntimeException("No resolver factory found for value prefix $prefix")
            val resolver = resolverFactory.createResolver(processInstanceId, variableScope)
            //Create a list of resolved Map entries
            requestedValues.mapNotNull { requestedValue ->
                resolver.apply(requestedValue.substringAfter(":"))
                    ?.let { requestedValue to it }
            }
        }.flatten().toMap()
    }

    /**
     * Handle values. Usually by storing them somewhere.
     *
     * @param processInstanceId The Camunda processInstanceId these values belong to
     * @param variableScope An implementation of VariableScope.
     * @param values mapOf(doc:add:/firstname to John)
     */
    fun handleValues(
        processInstanceId: ProcessInstanceId,
        variableScope: VariableScope,
        values: Map<String, Any>
    ) {
        values.entries
            .groupBy { it.key.substringBefore(":", missingDelimiterValue = "") }
            .forEach { (prefix, values) ->
                val resolverFactory = resolverFactoryMap[prefix]
                    ?: throw RuntimeException("No resolver factory found for value prefix $prefix")

                resolverFactory.handleValues(
                    processInstanceId,
                    variableScope,
                    values.associate { it.key.substringAfter(":") to it.value }
                )
            }
    }
}