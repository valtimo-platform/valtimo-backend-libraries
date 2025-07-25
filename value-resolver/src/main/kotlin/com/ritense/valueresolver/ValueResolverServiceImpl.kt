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

import com.ritense.logging.LoggableResource
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valueresolver.ValueResolverPropertyKey.Companion.DOCUMENT_ID
import com.ritense.valueresolver.ValueResolverPropertyKey.Companion.PROCESS_INSTANCE_ID
import com.ritense.valueresolver.ValueResolverPropertyKey.Companion.VARIABLE_SCOPE
import org.operaton.bpm.engine.delegate.VariableScope
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@SkipComponentScan
class ValueResolverServiceImpl(
    valueResolverFactories: List<ValueResolverFactory>
) : ValueResolverService {

    // This property is lazy because valueResolverFactories can contain Lazy proxy instances
    private val resolverFactoryMap: Map<String, ValueResolverFactory> by lazy {
        valueResolverFactories.groupBy { it.supportedPrefix() }
            .onEach { (key, value) ->
                if (value.size != 1) {
                    throw RuntimeException("Expected 1 resolver for prefix '$key'. Found: ${value.joinToString { resolver -> resolver.javaClass.simpleName }}")
                }
            }.map { (key, value) ->
                key to value.first()
            }.toMap()
    }

    override fun supportsValue(value: String): Boolean {
        return resolverFactoryMap.containsKey(getPrefix(value))
    }

    override fun getValueResolvers(): List<String> {
        return resolverFactoryMap.keys.filter { prefix -> prefix != "" }.toList()
    }

    override fun getResolvableKeys(
        request: ValueResolverOptionRequest,
        caseDefinitionKey: String
    ): List<ValueResolverOption> {
        val prefixes = request.prefixes.ifEmpty { resolverFactoryMap.keys }
        return prefixes.fold(emptyList()) { list, prefix ->
            val newOptions = resolverFactoryMap[prefix]?.getResolvableKeyOptions(caseDefinitionKey) ?: emptyList()
            list + newOptions.filter { option -> request.type.equals(option.type) }
        }
    }

    override fun getResolvableKeys(
        request: ValueResolverOptionRequest,
        caseDefinitionId: CaseDefinitionId
    ): List<ValueResolverOption> {
        val prefixes = request.prefixes.ifEmpty { resolverFactoryMap.keys }
        return prefixes.fold(emptyList()) { list, prefix ->
            val newOptions = resolverFactoryMap[prefix]?.getResolvableKeyOptions(caseDefinitionId) ?: emptyList()
            list + newOptions.filter { option -> request.type.equals(option.type) }
        }
    }

    /**
     * This method provides a way of resolving requestedValues into values using defined resolvers.
     * requestedValues are typically prefixed, like 'pv:propertyName'.
     * If not, a resolver should be configured to handle '' prefixes.
     *
     * A requestedValue can only be resolved when a resolver for that prefix is configured.
     * An unresolved requestedValue will not be included in the returned map.
     *
     * @param processInstanceId The Operaton processInstanceId these values belong to
     * @param variableScope An implementation of VariableScope. For instance: a TaskDelegate or DelegateExecution
     * @param requestedValues The requestedValues that should be resolved into values.
     * @return A map where the key is the requestedValue, and the value the resolved value.
     */
    override fun resolveValues(
        @LoggableResource("com.ritense.valtimo.operaton.domain.OperatonExecution") processInstanceId: String,
        variableScope: VariableScope,
        requestedValues: Collection<String>
    ): Map<String, Any?> {
        return resolveValues(
            properties = mapOf(PROCESS_INSTANCE_ID to processInstanceId, VARIABLE_SCOPE to variableScope),
            requestedValues = requestedValues
        )
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
        @LoggableResource("documentDefinitionName") documentDefinitionName: String,
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
        @LoggableResource("com.ritense.document.domain.impl.JsonSchemaDocument") documentInstanceId: String,
        requestedValues: Collection<String>
    ): Map<String, Any?> {
        return resolveValues(
            properties = mapOf(DOCUMENT_ID to documentInstanceId),
            requestedValues = requestedValues
        )
    }

    /**
     * This method provides a way of resolving requestedValues into values using defined resolvers.
     * requestedValues are typically prefixed, like 'pv:propertyName'.
     * If not, a resolver should be configured to handle 'pv' prefixes.
     *
     * A requestedValue can only be resolved when a resolver for that prefix is configured.
     * An unresolved requestedValue will not be included in the returned map.
     *
     * @param properties A map containing additional details about the value that needs to be resolved.
     * @param requestedValues The requestedValues that should be resolved into values. Can contain additional properties.
     * @return A map where the key is the requestedValue, and the value the resolved value.
     */
    override fun resolveValues(
        properties: Map<String, Any>,
        requestedValues: Collection<String>
    ): Map<String, Any?> {
        val allRequestedValues =
            (extractAdditionalRequestedValuesFromQueryParameters(requestedValues) + requestedValues).distinct()

        val resolvedValues = mutableMapOf<String, Any?>()
        toResolverFactoryGroups(allRequestedValues)
            .forEach { (resolverFactory, queryParamProperties, requestedValues) ->
                val resolvedProperties = (properties + queryParamProperties).entries.associate { (key, value) ->
                    key to (resolvedValues[value] ?: value)
                }
                val resolver = resolverFactory.createResolver(resolvedProperties)
                //Create a list of resolved Map entries
                requestedValues.forEach { requestedValue ->
                    resolvedValues[requestedValue] = resolver.apply(trimPrefix(trimQueryParameters(requestedValue)))
                }
            }
        return resolvedValues
    }

    /**
     * Handle values. Usually by storing them somewhere.
     *
     * @param processInstanceId The Operaton processInstanceId these values belong to
     * @param variableScope An implementation of VariableScope.
     * @param values mapOf(doc:add:/firstname to John)
     */
    override fun handleValues(
        @LoggableResource("com.ritense.valtimo.operaton.domain.OperatonExecution") processInstanceId: String,
        variableScope: VariableScope?,
        values: Map<String, Any?>
    ) {
        toResolverFactoryMap(values.keys).forEach { (resolverFactory, propertyPaths) ->

            resolverFactory.handleValues(
                processInstanceId,
                variableScope,
                mapPropertyPaths(propertyPaths, values)
            )
        }
    }

    override fun handleValues(
        @LoggableResource("com.ritense.document.domain.impl.JsonSchemaDocument") documentId: UUID,
        values: Map<String, Any?>
    ) {
        toResolverFactoryMap(values.keys).forEach { (resolverFactory, propertyPaths) ->

            resolverFactory.handleValues(
                documentId,
                mapPropertyPaths(propertyPaths, values)
            )
        }
    }

    override fun preProcessValuesForNewCase(
        values: Map<String, Any?>
    ): Map<String, Any> {
        return toResolverFactoryMap(values.keys).mapValues { (resolverFactory, propertyPaths) ->
            resolverFactory.preProcessValuesForNewCase(
                mapPropertyPaths(propertyPaths, values)
            )
        }.mapKeys { (resolverFactory, _) ->
            resolverFactory.supportedPrefix()
        }
    }

    private fun mapPropertyPaths(
        propertyPaths: List<String>,
        values: Map<String, Any?>
    ) = propertyPaths.associate { propertyPath -> trimPrefix(propertyPath) to values[propertyPath] }

    fun toResolverFactoryMap(requestedValues: Collection<String>): Map<ValueResolverFactory, List<String>> {
        //Group by prefix
        return requestedValues.groupBy(::getPrefix)
            .mapNotNull { (prefix, requestedValues) ->
                //Create a resolver per prefix group
                val resolverFactory = resolverFactoryMap[prefix]
                    ?: throw RuntimeException("No resolver factory found for value prefix $prefix")
                //Create a map of ValueResolverFactories
                resolverFactory to requestedValues
            }.toMap()
    }

    fun toResolverFactoryGroups(requestedValues: Collection<String>): List<Triple<ValueResolverFactory, Map<String, Any>, List<String>>> {
        // Group by prefix
        return requestedValues.groupBy(::getPrefix)
            .flatMap { (prefix, requestedValues) ->
                // Create a resolver per prefix group
                val resolverFactory = resolverFactoryMap[prefix]
                    ?: throw RuntimeException("No resolver factory found for value prefix $prefix")
                // Group by properties
                requestedValues
                    .groupBy { getQueryParameters(it) }
                    .map { (properties, requestedValues) ->
                        // Create a triple of [ValueResolverFactories, properties, requestedValues]
                        Triple(resolverFactory, properties, requestedValues)
                    }
            }
    }


    private fun getPrefix(value: String) = value.substringBefore(DELIMITER, missingDelimiterValue = "")
    private fun trimPrefix(value: String) = value.substringAfter(DELIMITER)

    private fun addPrefixToResolvableKeys(prefix: String, resolvableKeys: List<String>?): List<String> {
        return (resolvableKeys ?: emptyList()).map { "$prefix:$it" }
    }

    private fun extractAdditionalRequestedValuesFromQueryParameters(requestedValues: Collection<String>): List<String> {
        return requestedValues.flatMap { requestedValue ->
            extractAdditionalRequestedValuesFromQueryParameters(requestedValue)
        }
    }

    private fun extractAdditionalRequestedValuesFromQueryParameters(requestedValue: String): List<String> {
        return getQueryParameters(requestedValue).values
            .filter { value -> value.contains(':') && resolverFactoryMap.keys.contains(getPrefix(value)) }
    }

    private fun getQueryParameters(requestedValue: String): Map<String, String> {
        return Regex("[?&]([a-zA-Z0-9]+)=([^&]+)(?=&|$)")
            .findAll(requestedValue)
            .associate { it.groupValues[1] to it.groupValues[2] }
    }

    private fun trimQueryParameters(requestedValue: String): String =
        Regex("([^?]+)").find(requestedValue)!!.groupValues[1]

    companion object {
        const val DELIMITER = ":"
    }
}
