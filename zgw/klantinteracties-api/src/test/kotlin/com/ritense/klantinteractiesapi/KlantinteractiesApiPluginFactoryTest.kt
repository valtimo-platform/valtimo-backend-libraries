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

package com.ritense.klantinteractiesapi

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.klantinteractiesapi.client.KlantinteractiesApiClient
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProperty
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.zakenapi.ZaakUrlProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class KlantinteractiesApiPluginFactoryTest {
    @Test
    fun `should create KlantinteractiesApiPlugin`() {
        val pluginService: PluginService = mock()
        val klantinteractiesApiClient: KlantinteractiesApiClient = mock()
        val authentication: KlantinteractiesApiAuthentication = mock()
        whenever(pluginService.createInstance(any<PluginConfigurationId>())).thenReturn(authentication)
        whenever(pluginService.getObjectMapper()).thenReturn(MapperSingleton.get())

        val pluginProperties: String = """
            {
              "url": "http://klantinteracties.plugin.url",
              "authenticationPluginConfiguration": "da119c58-b350-42fe-b07a-b235c56b0d8f"
            }
        """.trimIndent()

        val factory = KlantinteractiesApiPluginFactory(pluginService, klantinteractiesApiClient)

        val pluginDefinition = createPluginDefinition()
        val pluginConfiguration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            MapperSingleton.get().readTree(pluginProperties) as ObjectNode,
            pluginDefinition
        )

        val plugin = factory.create(pluginConfiguration)

        assertEquals("http://klantinteracties.plugin.url", plugin.url.toString())
        assertEquals(authentication, plugin.authenticationPluginConfiguration)
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
                "url", pluginDefinition, "title", required = true,
                secret = false, "url", "java.net.URI"
            )
        )
        propertyDefinitions.add(
            PluginProperty(
                "authenticationPluginConfiguration", pluginDefinition, "title",
                required = true, secret = false, "authenticationPluginConfiguration",
                "com.ritense.klantinteractiesapi.KlantinteractiesApiAuthentication"
            )
        )

        return pluginDefinition
    }
}