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

import org.camunda.bpm.engine.delegate.VariableScope

class ValueResolverService(
    valueResolverFactories: List<ValueResolverFactory>
) {
    private val resolverFactoryMap: Map<String, ValueResolverFactory> = valueResolverFactories.groupBy { it.supportedPrefix() }
        .filter { (key, value) ->
            if(value.size != 1) {
                throw RuntimeException("Expected 1 resolver for prefix '$key'. Found: ${value.joinToString { resolver -> resolver.javaClass.simpleName }}")
            }
            true
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
        processInstanceId: String,
        variableScope: VariableScope,
        requestedValues: List<String>
    ): Map<String, Any> {
        return toResolverFactoryMap(requestedValues).map { (resolverFactory, requestedValues) ->
            val resolver = resolverFactory.createResolver(processInstanceId, variableScope)
            //Create a list of resolved Map entries
            requestedValues.mapNotNull { requestedValue ->
                resolver.apply(requestedValue.substringAfter(":"))
                    ?.let { requestedValue to it }
            }
        }.flatten().toMap()
    }


    /**
     * This method provides a way of validating a propertyName using defined resolvers.
     * requestedValues are typically prefixed, like 'pv:propertyName'.
     * If not, a resolver should be configured to handle 'pv' prefixes.
     *
     * If the resolver doesn't accept the propertyName, it will throw an error.
     *
     * @param documentInstanceId The documentInstanceId these values belong to.
     * @param requestedValues The requestedValues that should be validated.
     */
    fun validateValues(
        documentDefinitionName: String,
        requestedValues: List<String>
    ) {
        toResolverFactoryMap(requestedValues).forEach { (resolverFactory, requestedValues) ->
            val validator = resolverFactory.createValidator(documentDefinitionName)

            requestedValues.forEach { requestedValue ->
                validator.apply(requestedValue.substringAfter(":"))
            }
        }
    }

    /**
     * This method provides a way of resolving requestedValues into values using defined resolvers.
     * requestedValues are typically prefixed, like 'pv:propertyName'.
     * If not, a resolver should be configured to handle 'pv' prefixes.
     *
     * A requestedValue can only be resolved when a resolver for that prefix is configured.
     * An unresolved requestedValue will not be included in the returned map.
     *
     * @param documentInstanceId The documentInstanceId these values belong to
     * @param requestedValues The requestedValues that should be resolved into values.
     * @return A map where the key is the requestedValue, and the value the resolved value.
     */
    fun resolveValues(
        documentInstanceId: String,
        requestedValues: List<String>
    ): Map<String, Any> {
        return toResolverFactoryMap(requestedValues).map { (resolverFactory, requestedValues) ->
            val resolver = resolverFactory.createResolver(documentInstanceId)
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
        processInstanceId: String,
        variableScope: VariableScope?,
        values: Map<String, Any>
    ) {
        toResolverFactoryMap(values.keys).forEach { (resolverFactory, propertyPaths) ->

            resolverFactory.handleValues(
                processInstanceId,
                variableScope,
                propertyPaths.associate { propertyPath -> propertyPath.substringAfter(":") to values[propertyPath]!! }
            )
        }
    }

    private fun toResolverFactoryMap(requestedValues: Collection<String>): Map<ValueResolverFactory, List<String>> {
        //Group by prefix
        return requestedValues.groupBy {
            it.substringBefore(":", missingDelimiterValue = "")
        }.mapNotNull { (prefix, requestedValues) ->
            //Create a resolver per prefix group
            val resolverFactory = resolverFactoryMap[prefix]
                ?: throw RuntimeException("No resolver factory found for value prefix $prefix")
            //Create a map of ValueResolverFactories
            resolverFactory to requestedValues
        }.toMap()
    }
}
