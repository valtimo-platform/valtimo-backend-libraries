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
            resolverFactoryMap[prefix]?.createResolver(processInstanceId, variableScope)?.let { resolve ->
                //Create a list of resolved Map entries
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