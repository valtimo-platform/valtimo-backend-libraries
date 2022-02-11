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
    private val resolverFactoryMap = valueResolverFactories.associateBy { it.supportedPrefix() }

    /**
     * This method provides a way of resolving placeholders into values using defined resolvers.
     * Placeholders are typically prefixed, like 'pv:propertyName'.
     * If not, a resolver should be configured to handle '' prefixes.
     *
     * A placeholder can only be resolved when a resolver for that prefix is configured.
     * An unresolved placeholder will not be included in the returned map.
     *
     * @param processInstanceId The Camunda processInstanceId these values belong to
     * @param variableScope An implementation of VariableScope. For instance: a TaskDelegate or DelegateExecution
     * @param placeholders The placeholders that should be resolved into values.
     * @return A map where the key is the placeholder, and the value the resolved value.
     */
    fun resolvePlaceholders(
        processInstanceId: ProcessInstanceId,
        variableScope: VariableScope,
        placeholders: List<String>
    ): Map<String, Any> {
        //Group by prefix
        return placeholders.groupBy {
            it.substringBefore(":", missingDelimiterValue = "")
        }.mapNotNull { (prefix, placeholders) ->
            //Create a resolver per prefix group
            resolverFactoryMap[prefix]?.createResolver(processInstanceId, variableScope)
                //Create a list of resolved Map entries
                ?.let { resolve ->
                placeholders.mapNotNull { placeholder ->
                    resolve(placeholder.substringAfter(":"))
                        ?.let { placeholder to it }
                }
            }
        }.flatten().associate { (key, value) -> //Create a Map from a list of entries
            key to value
        }
    }
}