/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.zakenapi.service

import com.ritense.document.domain.Document
import com.ritense.document.domain.DocumentDefinition
import com.ritense.document.domain.event.DocumentCreatedEvent
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.events.PluginConfigurationDeletedEvent
import com.ritense.plugin.events.PluginConfigurationIdUpdatedEvent
import com.ritense.plugin.service.PluginService
import com.ritense.zakenapi.ZakenApiPlugin
import com.ritense.zakenapi.domain.ZaakTypeLink
import com.ritense.zakenapi.domain.ZaakTypeLinkId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.event.EventListener
import java.net.URI
import java.util.UUID
import kotlin.test.assertNull

class ZakenApiEventListenerTest {
    lateinit var pluginService: PluginService
    lateinit var zaakTypeLinkService: ZaakTypeLinkService
    lateinit var zakenApiEventListener: ZakenApiEventListener

    @BeforeEach
    fun setUp() {
        pluginService = mock<PluginService>()
        zaakTypeLinkService = mock<ZaakTypeLinkService>()
        zakenApiEventListener = ZakenApiEventListener(pluginService, zaakTypeLinkService)
    }

    @Test
    fun `should create zaak on DocumentCreatedEvent when zakenApiPluginConfigurationId is set`() {
        val pluginId = PluginConfigurationId.existingId(UUID.randomUUID())
        val zaakTypeLink = ZaakTypeLink(
            ZaakTypeLinkId.newId(UUID.randomUUID()),
            "name",
            URI("http://some-url"),
            true,
            pluginId.id,
            mock()
        )

        val event = setupDocumentCreatedRequest()
        val zakenApiPlugin = mock<ZakenApiPlugin>()
        val documentId = mock<Document.Id>()

        whenever(zaakTypeLinkService.get(any())).thenReturn(zaakTypeLink)
        whenever(pluginService.createInstance<ZakenApiPlugin>(any<UUID>())).thenReturn(zakenApiPlugin)
        whenever(event.documentId()).thenReturn(documentId)
        whenever(documentId.id).thenReturn(UUID.randomUUID())

        zakenApiEventListener.handle(event)

        verify(zakenApiPlugin).createZaak(any<UUID>(), any(), any(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `should not create zaak on DocumentCreatedEvent when zakenApiPluginConfigurationId is null`() {
        val pluginId = PluginConfigurationId.existingId(UUID.randomUUID())
        val zaakTypeLink = ZaakTypeLink(
            ZaakTypeLinkId.newId(UUID.randomUUID()),
            "name",
            URI("http://some-url"),
            true,
            null,
            mock()
        )

        val event = setupDocumentCreatedRequest()
        val zakenApiPlugin = mock<ZakenApiPlugin>()

        whenever(zaakTypeLinkService.get(any())).thenReturn(zaakTypeLink)
        whenever(pluginService.createInstance(any<PluginConfigurationId>())).thenReturn(zakenApiPlugin)

        zakenApiEventListener.handle(event)

        verify(zakenApiPlugin, never()).createZaak(any<UUID>(), any(), any(), any(), any(), any())
    }

    @Test
    fun `should not create zaak on DocumentCreatedEvent when createWithDossier is false`() {
        val pluginId = PluginConfigurationId.existingId(UUID.randomUUID())
        val zaakTypeLink = ZaakTypeLink(
            ZaakTypeLinkId.newId(UUID.randomUUID()),
            "name",
            URI("http://some-url"),
            false,
            pluginId.id,
            mock()
        )

        val event = setupDocumentCreatedRequest()
        val zakenApiPlugin = mock<ZakenApiPlugin>()

        whenever(zaakTypeLinkService.get(any())).thenReturn(zaakTypeLink)
        whenever(pluginService.createInstance(any<PluginConfigurationId>())).thenReturn(zakenApiPlugin)

        zakenApiEventListener.handle(event)

        verify(zakenApiPlugin, never()).createZaak(any<UUID>(), any(), any(), any(), any(), any())
    }

    @EventListener(DocumentCreatedEvent::class)
    fun handle(event: DocumentCreatedEvent) {
        val zaakTypeLink = zaakTypeLinkService.get(event.definitionId().name())
        zaakTypeLink?.let {
            if (it.createWithDossier && it.zakenApiPluginConfigurationId != null) {
                val zakenApiPlugin = pluginService.createInstance(it.zakenApiPluginConfigurationId!!) as ZakenApiPlugin
                zakenApiPlugin.createZaak(event.documentId().id, it.rsin!!, it.zaakTypeUrl)
            }
        }
    }


    @Test
    fun `should reset plugin configuration id on PluginConfigurationDeletedEvent`() {
        val pluginId = PluginConfigurationId.existingId(UUID.randomUUID())
        val pluginConfiguration = PluginConfiguration(pluginId, "name", mock(), mock())
        val event = PluginConfigurationDeletedEvent(pluginConfiguration)
        val zaakTypeLinkCaptor = argumentCaptor<ZaakTypeLink>()

        val zaakTypeLink = ZaakTypeLink(
            ZaakTypeLinkId.newId(UUID.randomUUID()),
            "name",
            URI("http://some-url"),
            true,
            pluginId.id,
            mock()
        )

        whenever(zaakTypeLinkService.getByPluginConfigurationId(any())).thenReturn(listOf(zaakTypeLink))

        zakenApiEventListener.handle(event)

        verify(zaakTypeLinkService).modify(zaakTypeLinkCaptor.capture())

        val updatedLink = zaakTypeLinkCaptor.firstValue
        assertNull(updatedLink.zakenApiPluginConfigurationId)
    }

    @Test
    fun `should change plugin configuration id on PluginConfigurationIdUpdatedEvent`() {
        val oldPluginId = PluginConfigurationId.existingId(UUID.randomUUID())
        val newPluginId = PluginConfigurationId.existingId(UUID.randomUUID())
        val pluginConfiguration = PluginConfiguration(newPluginId, "name", mock(), mock())
        val event = PluginConfigurationIdUpdatedEvent(newPluginId.id, oldPluginId.id, pluginConfiguration)
        val zaakTypeLinkCaptor = argumentCaptor<ZaakTypeLink>()

        val zaakTypeLink = ZaakTypeLink(
            ZaakTypeLinkId.newId(UUID.randomUUID()),
            "name",
            URI("http://some-url"),
            true,
            oldPluginId.id,
            mock()
        )

        whenever(zaakTypeLinkService.getByPluginConfigurationId(any())).thenReturn(listOf(zaakTypeLink))

        zakenApiEventListener.handle(event)

        verify(zaakTypeLinkService).modify(zaakTypeLinkCaptor.capture())

        val updatedLink = zaakTypeLinkCaptor.firstValue
        assertEquals(newPluginId.id, updatedLink.zakenApiPluginConfigurationId)
    }

    private fun setupDocumentCreatedRequest(): DocumentCreatedEvent {
        val event = mock<DocumentCreatedEvent>()
        val idMock = mock<DocumentDefinition.Id>()
        whenever(event.definitionId()).thenReturn(idMock)
        whenever(idMock.name()).thenReturn("test")
        return event
    }
}