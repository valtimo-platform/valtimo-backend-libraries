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

package com.ritense.catalogiapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProperty
import com.ritense.plugin.service.PluginService
import com.ritense.catalogiapi.client.CatalogiApiClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.net.URI

internal class CatalogiApiPluginFactoryTest {

    @Test
    fun `should create CatalogiApiPlugin`() {
        val pluginService = mock<PluginService>()
        val catalogiApiClient = mock<CatalogiApiClient>()
        val authenticationMock = mock<CatalogiApiAuthentication>()
        whenever(pluginService.createInstance(any<PluginConfigurationId>())).thenReturn(authenticationMock)
        whenever(pluginService.getObjectMapper()).thenReturn(jacksonObjectMapper())

        val factory = CatalogiApiPluginFactory(
            pluginService,
            catalogiApiClient
        )

        val catalogiApiPluginProperties: String = """
            {
              "url": "http://catalogi.plugin.url",
              "authenticationPluginConfiguration": "7c4e15e4-c245-4fd9-864b-dd36baa02abf"
            }
        """.trimIndent()

        val pluginDefinition = createPluginDefinition()
        val pluginConfiguration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            ObjectMapper().readTree(catalogiApiPluginProperties) as ObjectNode,
            pluginDefinition
        )
        val plugin = factory.create(pluginConfiguration)

        assertEquals(URI("http://catalogi.plugin.url"), plugin.url)
        assertEquals(authenticationMock, plugin.authenticationPluginConfiguration)
        assertEquals(catalogiApiClient, plugin.client)
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
            PluginProperty("authenticationPluginConfiguration", pluginDefinition, "title",
            required = true, secret = false, "authenticationPluginConfiguration",
            "com.ritense.catalogiapi.CatalogiApiAuthentication")
        )

        return pluginDefinition
    }
}
