/*
 *  Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.valtimo.camunda.domain

import org.camunda.bpm.engine.delegate.VariableScope
import org.camunda.bpm.engine.variable.value.TypedValue

abstract class CamundaVariableScope : VariableScope {

    protected abstract fun getVariableInstancesLocal() : Collection<CamundaVariableInstance>

    protected abstract fun getParentVariableScope() : CamundaVariableScope?

    override fun getVariables(): Map<String, Any?> = variablesTyped

    override fun getVariablesTyped(): CamundaVariableMap = getVariablesTyped(true)

    override fun getVariablesTyped(deserializeValues: Boolean): CamundaVariableMap {
        val parentEntries = getParentVariableScope()?.getVariablesTyped(deserializeValues)?.getEntriesTyped() ?: setOf()
        val localEntries = getVariablesLocalTyped(deserializeValues).getEntriesTyped()

        return CamundaVariableMap((localEntries + parentEntries).associate { it.key to it.value })
    }

    override fun getVariablesLocal(): Map<String, Any?> = variablesLocalTyped

    override fun getVariablesLocalTyped(): CamundaVariableMap = getVariablesLocalTyped(true)

    override fun getVariablesLocalTyped(deserializeValues: Boolean): CamundaVariableMap {
        return CamundaVariableMap(getVariableInstancesLocal().associate {
            it.name to it.getTypedValue(deserializeValues)
        })
    }

    override fun getVariable(variableName: String): Any? = variables[variableName]

    override fun getVariableLocal(variableName: String): Any? = variablesLocal[variableName]

    override fun <T : TypedValue> getVariableTyped(variableName: String): T? =
        getVariableTyped(variableName, true)

    override fun <T : TypedValue> getVariableTyped(variableName: String, deserializeValue: Boolean): T? =
        getVariablesTyped(deserializeValue)[variableName] as T?

    override fun <T : TypedValue> getVariableLocalTyped(variableName: String): T? =
        getVariableLocalTyped(variableName, true)

    override fun <T : TypedValue> getVariableLocalTyped(variableName: String, deserializeValue: Boolean): T? =
        getVariablesLocalTyped(deserializeValue)[variableName] as T?

    override fun getVariableNames(): Set<String> = variables.keys

    override fun getVariableNamesLocal(): Set<String> = variablesLocal.keys

    override fun setVariable(variableName: String, value: Any?) {
        throw UnsupportedOperationException("This implementation is read-only")
    }

    override fun setVariableLocal(variableName: String, value: Any?) {
        throw UnsupportedOperationException("This implementation is read-only")
    }

    override fun setVariables(variables: Map<String, Any?>?) {
        throw UnsupportedOperationException("This implementation is read-only")
    }

    override fun setVariablesLocal(variables: Map<String, Any?>?) {
        throw UnsupportedOperationException("This implementation is read-only")
    }

    override fun hasVariables(): Boolean = hasVariablesLocal() || variables.isNotEmpty()

    override fun hasVariablesLocal(): Boolean = variablesLocal.isNotEmpty()

    override fun hasVariable(variableName: String): Boolean =
        hasVariableLocal(variableName) || variables.containsKey(variableName)

    override fun hasVariableLocal(variableName: String): Boolean = variablesLocal.containsKey(variableName)

    override fun removeVariable(variableName: String) {
        throw UnsupportedOperationException("This implementation is read-only")
    }

    override fun removeVariableLocal(variableName: String) {
        throw UnsupportedOperationException("This implementation is read-only")
    }

    override fun removeVariables(variableNames: Collection<String>?) {
        throw UnsupportedOperationException("This implementation is read-only")
    }

    override fun removeVariables() {
        throw UnsupportedOperationException("This implementation is read-only")
    }

    override fun removeVariablesLocal(variableNames: Collection<String>?) {
        throw UnsupportedOperationException("This implementation is read-only")
    }

    override fun removeVariablesLocal() {
        throw UnsupportedOperationException("This implementation is read-only")
    }


}