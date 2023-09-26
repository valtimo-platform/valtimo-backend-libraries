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

import com.ritense.valueresolver.exception.ValueResolverValidationException
import java.util.UUID
import java.util.function.Function
import org.camunda.bpm.engine.delegate.VariableScope

/**
 * A factory that creates a value resolver for a specific prefix.
 */
interface ValueResolverFactory {

    /**
     * Returns the requestedValue prefix that the resolver can handle.
     * For example, to resolve 'pv:someProperty', this method should return 'pv'
     *
     * @return The requestedValue prefix (for example: 'pv')
     */
    fun supportedPrefix(): String

    /**
     * This creates a requestedValue resolver within a certain context.
     * The returned resolver can be called multiple times within the same context for different requestedValues.
     *
     * We can use this strategy to limit the amount of calls to an external source, which is a performance benefit.
     *
     * The requestedValue argument of the returned resolver is already stripped of the prefix:
     * 'someProperty' will be passed as an argument when the original requestedValue was 'pv:someProperty'
     *
     * @param processInstanceId The Camunda processInstanceId these values belong to
     * @param variableScope An implementation of VariableScope. For instance: a TaskDelegate or DelegateExecution
     *
     * @return a resolver that handles one requestedValue at a time within the same context.
     */
    fun createResolver(processInstanceId: String, variableScope: VariableScope)
        : Function<String, Any?>

    /**
     * This creates a property validator within a certain context.
     * The validator will throw and exception when the property is invalid
     * The returned validator can be called multiple times within the same context for different properties.
     *
     * We can use this strategy to limit the amount of calls to an external source, which is a performance benefit.
     *
     * The path argument of the returned resolver is already stripped of the prefix:
     * 'someProperty' will be passed as an argument when the original requestedValue was 'pv:someProperty'
     *
     * @param documentDefinitionName The name of the document-definition that these properties belong to
     *
     * @return a resolver that handles one requestedValue at a time within the same context.
     */
    @Throws(ValueResolverValidationException::class)
    fun createValidator(documentDefinitionName: String)
            : Function<String, Unit> = Function { }

    /**
     * This creates a requestedValue resolver within a certain context.
     * The returned resolver can be called multiple times within the same context for different requestedValues.
     *
     * We can use this strategy to limit the amount of calls to an external source, which is a performance benefit.
     *
     * The requestedValue argument of the returned resolver is already stripped of the prefix:
     * 'someProperty' will be passed as an argument when the original requestedValue was 'pv:someProperty'
     *
     * @param documentId The documentId these values belong to
     *
     * @return a resolver that handles one requestedValue at a time within the same context.
     */
    fun createResolver(documentId: String)
        : Function<String, Any?>

    /**
     * @param processInstanceId The Camunda processInstanceId these values belong to
     * @param variableScope An implementation of VariableScope.
     * @param values The values to handle. i.e. mapOf(doc:add:/firstname to John)
     */
    fun handleValues(processInstanceId: String, variableScope: VariableScope?, values: Map<String, Any>)

    /**
     * Handle values for a case where a process is not relevant or present in the current context.
     *
     * @param documentId The id of the document these values belong to
     * @param values The values to handle. i.e. mapOf(doc:add:/firstname to John)
     */
    fun handleValues(documentId: UUID, values: Map<String, Any>) {
        //empty default method for backwards compatibility
    }

    /**
     * Processes and transforms values for use externally. This is used when case or process don't exist yet. For example
     * when creating a new case.
     *
     * @param values The values to handle. i.e. mapOf(doc:add:/firstname to John)
     */
    fun preProcessValuesForNewCase(values: Map<String, Any>): Any {
        return values
    }
}
