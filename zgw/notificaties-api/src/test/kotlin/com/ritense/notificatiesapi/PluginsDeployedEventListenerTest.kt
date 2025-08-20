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
import com.ritense.notificatiesapi.domain.Abonnement
import com.ritense.notificatiesapi.domain.NotificatiesApiAbonnementLink
import com.ritense.notificatiesapi.domain.NotificatiesApiConfigurationId
import com.ritense.notificatiesapi.exception.NotificatiesApiAbonnementException
import com.ritense.notificatiesapi.repository.NotificatiesApiAbonnementLinkRepository
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.service.PluginService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationContext
import java.net.URI
import java.util.UUID

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

        pluginsDeployedEventListener = PluginsDeployedEventListener(
            client,
            notificatiesApiAbonnementLinkRepository,
            pluginService,
            true
        )
        pluginsDeployedEventListener.setApplicationContext(context)
    }

    @Test
    fun `should register nothing`() {
        whenever(pluginService.getPluginConfigurations(any())).thenReturn(
            emptyList()
        )

        assertDoesNotThrow { pluginsDeployedEventListener.registerAbonnementenForNotificatiesApiPlugins() }
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


    @Test
    fun `should delete old abonnement that API does not have`() {
        val listenerInstance: NotificatiesApiListener = mock()

        val notificatiesApiPlugin: NotificatiesApiPlugin = mock()

        val configurationId = NotificatiesApiConfigurationId.existingId(
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        )
        val existingAbonnementLink = NotificatiesApiAbonnementLink(
            configurationId,
            "http://localhost:9999/nothing/123",
            "test"
        )

        whenever(client.getAbonnementen(any(), any()))
            .thenReturn(emptyList())
        whenever(notificatiesApiPlugin.url)
            .thenReturn(URI("http://localhost:9999/nothing"))
        whenever(notificatiesApiPlugin.notificatiesApiConfigurationId)
            .thenReturn(configurationId)
        whenever(listenerInstance.getNotificatiesApiPlugin())
            .thenReturn(notificatiesApiPlugin)
        whenever(pluginService.createInstance(any<PluginConfiguration>()))
            .thenReturn(listenerInstance)
        whenever(pluginService.getPluginConfigurations(any()))
            .thenReturn(listOf(mock()))
        whenever(notificatiesApiAbonnementLinkRepository.findAll())
            .thenReturn(listOf(existingAbonnementLink))
        whenever(notificatiesApiPlugin.authenticationPluginConfiguration)
            .thenReturn(mock())
        whenever(notificatiesApiPlugin.callbackUrl)
            .thenReturn(URI("http://localhost:9999/callback"))
        whenever(client.createAbonnement(any(), any(), any<Abonnement>()))
            .thenReturn(Abonnement(
                "http://localhost:9999/nothing/456",
                "http://localhost:9999/callback",
                "test",
                emptyList()
            ))

        pluginsDeployedEventListener.registerAbonnementenForNotificatiesApiPlugins()

        verify(notificatiesApiAbonnementLinkRepository).delete(any())
        verify(notificatiesApiAbonnementLinkRepository).save(any())
        verify(client).createAbonnement(any(), any(), any<Abonnement>())
    }

}