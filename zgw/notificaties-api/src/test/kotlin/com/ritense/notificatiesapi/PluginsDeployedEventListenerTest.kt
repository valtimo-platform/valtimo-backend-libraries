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

package com.ritense.notificatiesapi

import com.ritense.notificatiesapi.client.NotificatiesApiClient
import com.ritense.notificatiesapi.exception.NotificatiesApiAbonnementException
import com.ritense.notificatiesapi.repository.NotificatiesApiAbonnementLinkRepository
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.service.PluginService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationContext
import java.net.URI

class PluginsDeployedEventListenerTest {
    lateinit var client: NotificatiesApiClient
    lateinit var notificatiesApiAbonnementLinkRepository: NotificatiesApiAbonnementLinkRepository
    lateinit var pluginService: PluginService
    lateinit var context: ApplicationContext
    lateinit var pluginsDeployedEventListener: PluginsDeployedEventListener

    @BeforeEach
    fun setup() {
        client = mock()
        notificatiesApiAbonnementLinkRepository = mock()
        pluginService = mock()
        context = mock()

        pluginsDeployedEventListener =
            PluginsDeployedEventListener(client, notificatiesApiAbonnementLinkRepository, pluginService)
        pluginsDeployedEventListener.setApplicationContext(context)
    }

    @Test
    fun `should register nothing`() {
        whenever(pluginService.getPluginConfigurations(any())).thenReturn(
            emptyList()
        )

        pluginsDeployedEventListener.registerAbonnementenForNotificatiesApiPlugins()
    }

    @Test
    fun `should shutdown due to inability to connect to abonnementen api`() {
        val pluginInstance: NotificatiesApiListener = mock()

        val notificatiesApiPlugin: NotificatiesApiPlugin = mock()

        whenever(client.getAbonnementen(any(), any()))
            .thenAnswer { _ -> Exception() }
        whenever(notificatiesApiPlugin.url)
            .thenReturn(URI("http://localhost:9999/nothing"))
        whenever(pluginInstance.getNotificatiesApiPlugin())
            .thenReturn(notificatiesApiPlugin)
        whenever(pluginService.createInstance(any<PluginConfiguration>()))
            .thenReturn(pluginInstance)
        whenever(pluginService.getPluginConfigurations(any()))
            .thenReturn(listOf(mock()))

        assertThrows<NotificatiesApiAbonnementException> {
            pluginsDeployedEventListener.registerAbonnementenForNotificatiesApiPlugins()
        }
    }

}