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

package com.ritense.formviewmodel.validation

import com.fasterxml.jackson.databind.JsonNode

object FormIOFormPropertiesExtractor {
    fun extractProperties(formDefinition: JsonNode): List<String> {
        val componentsNode = formDefinition["components"]

        val keysWithNesting = mutableListOf<String>()

        fun extractKeys(node: JsonNode, prefix: String = "") {
            for (element in node) {
                val key = element["key"].asText()
                if (key == "submit") {
                    continue
                }
                val nestedComponentsNode = element["components"]
                if (nestedComponentsNode != null) {
                    extractKeys(nestedComponentsNode, if (prefix.isEmpty()) key else "$prefix.$key")
                } else {
                    if (prefix.isNotEmpty()) {
                        keysWithNesting.add("$prefix.$key")
                    } else {
                        keysWithNesting.add(key)
                    }
                }
            }
        }

        extractKeys(componentsNode)

        return keysWithNesting
    }
}