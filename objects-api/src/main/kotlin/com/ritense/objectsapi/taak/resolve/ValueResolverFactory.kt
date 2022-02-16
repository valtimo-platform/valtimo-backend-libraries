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
    fun createResolver(processInstanceId: ProcessInstanceId, variableScope: VariableScope)
        : Function<String, Any?>
}