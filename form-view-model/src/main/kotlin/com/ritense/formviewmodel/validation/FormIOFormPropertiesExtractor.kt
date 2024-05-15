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