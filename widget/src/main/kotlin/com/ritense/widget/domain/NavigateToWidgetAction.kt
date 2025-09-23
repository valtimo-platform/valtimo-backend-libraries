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

package com.ritense.widget.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("navigateTo")
data class NavigateToWidgetAction(
    val name: String?,
    val navigateTo: String,
) : WidgetAction {

    @JsonIgnore
    override fun getUnresolvedValues(): List<String> = getPlaceholders()
        .map { it.second }
        .filter { it.matches("[a-z].*".toRegex()) }

    override fun getExposedValues(resolveValue: (String) -> Any?): Map<String, Any?> {
        var resolvedLink = navigateTo
        getPlaceholders().forEach { (placeholder, placeholderValue) ->
            val resolvedPlaceholder = resolveValue(placeholderValue)
            if (resolvedPlaceholder == null) {
                return emptyMap()
            }
            resolvedLink = if (resolvedPlaceholder is Collection<*>) {
                resolvedLink.replace(placeholder, resolvedPlaceholder.joinToString(separator = ""))
            } else {
                resolvedLink.replace(placeholder, resolvedPlaceholder.toString())
            }
        }
        return mapOf(navigateTo to resolvedLink)
    }

    private fun getPlaceholders(): List<Pair<String, String>> {
        return Regex("\\$\\{([^\\}]+)\\}").findAll(navigateTo)
            .map { it.groupValues[0] to it.groupValues[1] }
            .toList()
    }
}
