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

package com.ritense.notificatiesapi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.notificatiesapi.client.NotificatiesApiClient
import com.ritense.notificatiesapi.repository.NotificatiesApiAbonnementLinkRepository
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProperty
import com.ritense.plugin.service.PluginService
import java.net.URI
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class NotificatiesApiPluginFactoryTest {

    @Test
    fun `should create NotificatiesApiPlugin`() {
        val pluginService = mock<PluginService>()
        val client = mock<NotificatiesApiClient>()
        val abonnementLinkRepository = mock<NotificatiesApiAbonnementLinkRepository>()

        whenever(pluginService.getObjectMapper()).thenReturn(jacksonObjectMapper())

        val factory = NotificatiesApiPluginFactory(
            pluginService,
            client,
            abonnementLinkRepository
        )

        val notificatiesApiPluginProperties: String = """
            {
              "url": "http://notificaties.plugin.url"
            }
        """.trimIndent()

        val pluginDefinition = createPluginDefinition()
        val pluginConfiguration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            jacksonObjectMapper()
                .readTree(notificatiesApiPluginProperties)
                .deepCopy(),
            pluginDefinition
        )
        val plugin = factory.create(pluginConfiguration)

        assertEquals(URI("http://notificaties.plugin.url"), plugin.url)
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

        return pluginDefinition
    }
}