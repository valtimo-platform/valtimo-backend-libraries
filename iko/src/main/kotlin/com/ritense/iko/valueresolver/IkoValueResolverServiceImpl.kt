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

package com.ritense.iko.valueresolver

import com.ritense.iko.IkoValueResolverFactory
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valueresolver.IkoValueResolverService
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
@SkipComponentScan
class IkoValueResolverServiceImpl(
    valueResolverFactories: List<IkoValueResolverFactory>
) : IkoValueResolverService {

    private val resolverFactoryMap: Map<String, IkoValueResolverFactory> by lazy {
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

    /**
     * This method provides a way of resolving requestedValues into values using defined resolvers.
     * requestedValues are typically prefixed, like 'pv:propertyName'.
     * If not, a resolver should be configured to handle '' prefixes.
     *
     * A requestedValue can only be resolved when a resolver for that prefix is configured.
     * An unresolved requestedValue will not be included in the returned map.
     *
     * @param context A list containing context
     * @param requestedValues The requestedValues that should be resolved into values.
     * @return A map where the key is the requestedValue, and the value the resolved value.
     */
    override fun resolveValues(
        context: List<Any>,
        requestedValues: Collection<String>,
        pageable: Pageable,
    ): Map<String, Any?> {
        return toResolverFactoryMap(requestedValues).map { (resolverFactory, requestedValues) ->
            val resolver = resolverFactory.createResolver(context, pageable)
            //Create a list of resolved Map entries
            requestedValues.map { requestedValue ->
                requestedValue to resolver.apply(trimPrefix(requestedValue))
            }
        }.flatten().toMap()
    }

    private fun toResolverFactoryMap(requestedValues: Collection<String>): Map<IkoValueResolverFactory, List<String>> {
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


    private fun getPrefix(value: String) = value.substringBefore(DELIMITER, missingDelimiterValue = "")
    private fun trimPrefix(value: String) = value.substringAfter(DELIMITER)

    companion object {
        const val DELIMITER = ":"
    }
}
