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

package com.ritense.valtimo.camunda.domain

import java.util.Collections
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.context.VariableContext
import org.camunda.bpm.engine.variable.value.TypedValue

class CamundaVariableMap(
    private val typedMap: Map<String, TypedValue?>
) : VariableMap,
    MutableMap<String, Any?> by Collections.unmodifiableMap(typedMap.mapValues { it.value?.value }) {


    fun getEntriesTyped() = typedMap.entries

    override fun putValue(name: String, value: Any?): VariableMap {
        throw UnsupportedOperationException("This implementation is read-only")
    }

    override fun putValueTyped(name: String, value: TypedValue?): VariableMap {
        throw UnsupportedOperationException("This implementation is read-only")
    }

    override fun <T : Any> getValue(name: String, type: Class<T>): T? {
        val value = this[name] ?: return null
        if (type.isInstance(value)) {
            return value as T
        } else {
            throw ClassCastException("Cannot cast variable named '$name' with value '$value`' to type '$type'.")
        }
    }

    override fun <T : TypedValue> getValueTyped(name: String): T? =
        (typedMap[name] ?: Variables.untypedNullValue()) as T?

    override fun asVariableContext(): VariableContext = CamundaVariableContext(typedMap)


}