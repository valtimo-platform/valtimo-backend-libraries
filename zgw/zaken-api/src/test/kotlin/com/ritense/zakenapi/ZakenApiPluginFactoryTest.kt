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

package com.ritense.zakenapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProperty
import com.ritense.plugin.service.PluginService
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.zakenapi.client.ZakenApiClient
import com.ritense.zakenapi.repository.ZaakInstanceLinkRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

internal class ZakenApiPluginFactoryTest {

    @Test
    fun `should create ZakenApiPlugin`() {
        val pluginService: PluginService = mock()
        val authenticationMock = mock<ZakenApiAuthentication>()
        whenever(pluginService.createInstance(any<PluginConfigurationId>())).thenReturn(authenticationMock)
        whenever(pluginService.getObjectMapper()).thenReturn(jacksonObjectMapper())

        val client: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val storageService: TemporaryResourceStorageService = mock()
        val zaakInstanceLinkRepository: ZaakInstanceLinkRepository = mock()

        val factory = ZakenApiPluginFactory(
            pluginService,
            client,
            zaakUrlProvider,
            storageService,
            zaakInstanceLinkRepository,
        )
        val zakenApiPluginProperties: String = """
            {
              "url": "http://zaken.plugin.url",
              "authenticationPluginConfiguration": "7c4e15e4-c245-4fd9-864b-dd36baa02abf"
            }
        """.trimIndent()

        val pluginDefinition = createPluginDefinition()
        val pluginConfiguration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            ObjectMapper().readTree(zakenApiPluginProperties) as ObjectNode,
            pluginDefinition
        )

        val plugin = factory.create(pluginConfiguration)

        assertEquals("http://zaken.plugin.url", plugin.url.toString())
        assertEquals(authenticationMock, plugin.authenticationPluginConfiguration)
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
            secret = false, "url","java.net.URI"))
        propertyDefinitions.add(PluginProperty("authenticationPluginConfiguration", pluginDefinition, "title",
            required = true, secret = false, "authenticationPluginConfiguration",
            "com.ritense.zakenapi.ZakenApiAuthentication"))

        return pluginDefinition
    }
}
