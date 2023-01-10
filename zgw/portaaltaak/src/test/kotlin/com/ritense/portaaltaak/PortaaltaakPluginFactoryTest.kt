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

package com.ritense.portaaltaak

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProperty
import com.ritense.plugin.service.PluginService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever


internal class PortaaltaakPluginFactoryTest {

    @Test
    fun `should create PortaaltaakPlugin`() {
        val pluginService = mock<PluginService>()
        whenever(pluginService.getObjectMapper()).thenReturn(jacksonObjectMapper())

        val factory = PortaaltaakPluginFactory(
            pluginService
        )

        val portaaltaakPluginProperties: String = """
            {
              "openNotificatiesPluginConfigurationUuid": "4d9e7fe7-0671-4955-a106-fc71dc7527a6"
            }
        """.trimIndent()
        val pluginDefinition = createPluginDefinition()
        val pluginConfiguration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            jacksonObjectMapper()
                .readTree(portaaltaakPluginProperties)
                .deepCopy(),
            pluginDefinition
        )
        val plugin = factory.create(pluginConfiguration)

        assertEquals("4d9e7fe7-0671-4955-a106-fc71dc7527a6", plugin.openNotificatiesPluginConfigurationUuid.toString())
    }

    private fun createPluginDefinition(): PluginDefinition {
        val propertyDefinitions = mutableSetOf<PluginProperty>()
        val pluginDefinition = PluginDefinition(
            "key",
            "title",
            "description",
            "class",
            propertyDefinitions
        )

        propertyDefinitions.add(
            PluginProperty(
                "openNotificatiesPluginConfigurationUuid", pluginDefinition, "title", required = true,
                secret = false, "openNotificatiesPluginConfigurationUuid", "java.util.UUID"
            )
        )

        return pluginDefinition
    }
}