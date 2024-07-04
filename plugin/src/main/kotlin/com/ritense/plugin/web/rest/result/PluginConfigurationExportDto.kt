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

package com.ritense.plugin.web.rest.result

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.plugin.domain.PluginConfiguration
import java.util.Locale
import java.util.UUID

data class PluginConfigurationExportDto(
    val id: UUID,
    val title: String,
    val pluginDefinitionKey: String,
    val properties: ObjectNode?
) {
    companion object {
        fun of(pluginConfiguration: PluginConfiguration): PluginConfigurationExportDto {
            return PluginConfigurationExportDto(
                id = pluginConfiguration.id.id,
                title = pluginConfiguration.title,
                pluginDefinitionKey = pluginConfiguration.pluginDefinition.key,
                properties = pluginConfiguration.properties?.let {
                    val configurationProperties: ObjectNode = it.deepCopy()

                    val secretDefinitionProperties = pluginConfiguration.pluginDefinition.properties
                        .filter { it.secret }
                        .map { it.fieldName }

                    configurationProperties.remove(secretDefinitionProperties)
                    secretDefinitionProperties.forEach {
                        val secretName = toSnakeUpperCase("${pluginConfiguration.title}_$it")
                        configurationProperties.put(it, "\${$secretName}")
                    }

                    configurationProperties
                }
            )
        }

        private fun toSnakeUpperCase(camelCase: String): String {
            return camelCase
                .replace("[^a-zA-Z0-9]+".toRegex(), "_")
                .replace("([a-z])([A-Z]+)".toRegex(), "$1_$2")
                .uppercase(Locale.getDefault())
        }
    }
}