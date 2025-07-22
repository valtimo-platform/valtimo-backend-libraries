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

import org.springframework.data.domain.Pageable

interface IkoValueResolverService {

    /**
     * This method provides a way of resolving requestedValues into values using defined resolvers.
     * requestedValues are typically prefixed, like 'pv:propertyName'.
     * If not, a resolver should be configured to handle 'pv' prefixes.
     *
     * A requestedValue can only be resolved when a resolver for that prefix is configured.
     * An unresolved requestedValue will not be included in the returned map.
     *
     * @param context A list containing context
     * @param requestedValues The requestedValues that should be resolved into values.
     * @return A map where the key is the requestedValue, and the value the resolved value.
     */
    fun resolveValues(
        context: Map<String, Any?>,
        requestedValues: Collection<String>,
        pageable: Pageable,
    ): Map<String, Any?>

    fun supportsValue(value: String): Boolean

    fun getValueResolvers(): List<String>
}