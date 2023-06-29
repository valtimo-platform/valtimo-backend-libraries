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

abstract class AbstractVariableScope {

    abstract fun getVariable(variableName: String): Any?

    abstract fun getVariableInstancesLocal(variableNames: Collection<String>?): Collection<CamundaVariableInstance>

    abstract fun getParentVariableScope(): AbstractVariableScope?

    open fun getVariables(variableNames: Collection<String>?): Map<String, Any?> {
        val allVariables = mutableMapOf<String, Any?>()
        val collectAll = variableNames == null
        val localVariables = getVariableInstancesLocal(variableNames)
        for (variable in localVariables) {
            if (!allVariables.containsKey(variable.name) && (collectAll || variableNames!!.contains(variable.name))) {
                allVariables[variable.name] = variable.getTypedValue()?.value
            }
        }

        if (collectAll || allVariables.keys != variableNames) {
            val parent = getParentVariableScope()
            if (parent != null) {
                allVariables.putAll(parent.getVariables(variableNames))
            }
        }
        return allVariables
    }

}