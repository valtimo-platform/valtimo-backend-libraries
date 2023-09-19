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

import java.util.UUID
import org.camunda.bpm.engine.delegate.VariableScope

open class ValueResolverServiceImpl(
    private val valueResolverFactories: List<ValueResolverFactory>
) : ValueResolverService {

    private lateinit var resolverFactoryMap: Map<String, ValueResolverFactory>
    fun getResolverFactoryMap() = if (this::resolverFactoryMap.isInitialized) {
        resolverFactoryMap
    } else {
        valueResolverFactories.groupBy { it.supportedPrefix() }
            .onEach { (key, value) ->
                if(value.size != 1) {
                    throw RuntimeException("Expected 1 resolver for prefix '$key'. Found: ${value.joinToString { resolver -> resolver.javaClass.simpleName }}")
                }
            }.map { (key, value) ->
                key to value.first()
            }.toMap()
    }

    override fun supportsValue(value: String) : Boolean {
        return getResolverFactoryMap().containsKey(getPrefix(value))
    }

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
    override fun resolveValues(
        processInstanceId: String,
        variableScope: VariableScope,
        requestedValues: Collection<String>
    ): Map<String, Any> {
        return toResolverFactoryMap(requestedValues).map { (resolverFactory, requestedValues) ->
            val resolver = resolverFactory.createResolver(processInstanceId, variableScope)
            //Create a list of resolved Map entries
            requestedValues.mapNotNull { requestedValue ->
                resolver.apply(trimPrefix(requestedValue))
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
     * @param documentDefinitionName The documentInstanceId these values belong to.
     * @param requestedValues The requestedValues that should be validated.
     */
    override fun validateValues(
        documentDefinitionName: String,
        requestedValues: List<String>
    ) {
        toResolverFactoryMap(requestedValues).forEach { (resolverFactory, requestedValues) ->
            val validator = resolverFactory.createValidator(documentDefinitionName)

            requestedValues.forEach { requestedValue ->
                validator.apply(trimPrefix(requestedValue))
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
    override fun resolveValues(
        documentInstanceId: String,
        requestedValues: Collection<String>
    ): Map<String, Any> {
        return toResolverFactoryMap(requestedValues).map { (resolverFactory, requestedValues) ->
            val resolver = resolverFactory.createResolver(documentInstanceId)
            //Create a list of resolved Map entries
            requestedValues.mapNotNull { requestedValue ->
                resolver.apply(trimPrefix(requestedValue))
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
    override fun handleValues(
        processInstanceId: String,
        variableScope: VariableScope?,
        values: Map<String, Any>
    ) {
        toResolverFactoryMap(values.keys).forEach { (resolverFactory, propertyPaths) ->

            resolverFactory.handleValues(
                processInstanceId,
                variableScope,
                propertyPaths.associate { propertyPath -> trimPrefix(propertyPath) to values[propertyPath]!! }
            )
        }
    }

    override fun handleValues(
        documentId: UUID,
        values: Map<String, Any>
    ) {
        toResolverFactoryMap(values.keys).forEach { (resolverFactory, propertyPaths) ->

            resolverFactory.handleValues(
                documentId,
                propertyPaths.associate { propertyPath -> trimPrefix(propertyPath) to values[propertyPath]!! }
            )
        }
    }

    override fun preProcessValuesForNewCase(
        values: Map<String, Any>
    ): Map<String, Any> {
        return toResolverFactoryMap(values.keys).mapValues { (resolverFactory, propertyPaths) ->
            resolverFactory.preProcessValuesForNewCase(
                propertyPaths.associate { propertyPath -> trimPrefix(propertyPath) to values[propertyPath]!! }
            )
        }.mapKeys { (resolverFactory, _) ->
            resolverFactory.supportedPrefix()
        }
    }

    private fun toResolverFactoryMap(requestedValues: Collection<String>): Map<ValueResolverFactory, List<String>> {
        //Group by prefix
        return requestedValues.groupBy(::getPrefix)
            .mapNotNull { (prefix, requestedValues) ->
                //Create a resolver per prefix group
                val resolverFactory = getResolverFactoryMap()[prefix]
                    ?: throw RuntimeException("No resolver factory found for value prefix $prefix")
                //Create a map of ValueResolverFactories
                resolverFactory to requestedValues
            }.toMap()
    }

    private fun getPrefix(value:String) = value.substringBefore(DELIMITER, missingDelimiterValue = "")
    private fun trimPrefix(value:String) = value.substringAfter(DELIMITER)

    companion object {
        const val DELIMITER = ":"
    }
}
