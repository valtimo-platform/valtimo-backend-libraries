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

package com.ritense.besluitenapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProperty
import com.ritense.plugin.service.PluginService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class BesluitenApiPluginFactoryTest {
    @Test
    fun `should create BesluitenApiPlugin`() {
        val pluginService: PluginService = mock()
        val authentication: BesluitenApiAuthentication = mock();
        whenever(pluginService.createInstance(any<PluginConfigurationId>())).thenReturn(authentication)
        whenever(pluginService.getObjectMapper()).thenReturn(jacksonObjectMapper())

        val pluginProperties: String = """
            {
              "url": "http://besluiten.plugin.url",
              "rsin": "051845623",
              "authenticationPluginConfiguration": "7c4e15e4-c245-4fd9-864b-dd36baa02abf"
            }
        """.trimIndent()

        val factory = BesluitenApiPluginFactory(pluginService)

        val pluginDefinition = createPluginDefinition()
        val pluginConfiguration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            ObjectMapper().readTree(pluginProperties) as ObjectNode,
            pluginDefinition
        )

        val plugin = factory.create(pluginConfiguration)

        assertEquals("http://besluiten.plugin.url", plugin.url.toString())
        assertEquals("051845623", plugin.rsin.toString())
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
            PluginProperty("url", pluginDefinition, "title", required = true,
                secret = false, "url","java.net.URI")
        )
        propertyDefinitions.add(
            PluginProperty("rsin", pluginDefinition, "title", required = true,
            secret = false, "rsin","com.ritense.zgw.Rsin")
        )
        propertyDefinitions.add(
            PluginProperty("authenticationPluginConfiguration", pluginDefinition, "title",
            required = true, secret = false, "authenticationPluginConfiguration",
            "com.ritense.besluitenapi.BesluitenApiAuthentication")
        )

        return pluginDefinition
    }
}