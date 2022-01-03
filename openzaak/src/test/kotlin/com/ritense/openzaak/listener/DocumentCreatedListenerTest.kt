/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
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

package com.ritense.openzaak.listener

import com.ritense.document.domain.Document
import com.ritense.document.domain.DocumentDefinition
import com.ritense.document.domain.event.DocumentCreatedEvent
import com.ritense.openzaak.domain.mapping.impl.ServiceTaskHandlers
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLink
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLinkId
import com.ritense.openzaak.service.impl.ZaakService
import com.ritense.openzaak.service.impl.ZaakTypeLinkService
import java.net.URI
import java.util.UUID
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

internal class DocumentCreatedListenerTest {

    val zaakService = mock(ZaakService::class.java)
    val zaakTypeLinkService = mock(ZaakTypeLinkService::class.java)
    val documentCreatedListener = DocumentCreatedListener(zaakService, zaakTypeLinkService)

    @Test
    fun `should create zaak when link type is found`() {
        `when`(zaakTypeLinkService.get("test")).thenReturn(ZaakTypeLink(
            ZaakTypeLinkId.newId(UUID.randomUUID()),
            "name",
            URI("http://some-url"),
            ServiceTaskHandlers(),
            true
        ))

        val event = mock(DocumentCreatedEvent::class.java)
        val documentDefinitionId = mock(DocumentDefinition.Id::class.java)
        val documentId = mock(Document.Id::class.java)
        `when`(event.definitionId()).thenReturn(documentDefinitionId)
        `when`(event.documentId()).thenReturn(documentId)
        `when`(documentDefinitionId.name()).thenReturn("test")

        documentCreatedListener.handle(event)

        verify(zaakService).createZaakWithLink(documentId)
    }

    @Test
    fun `should not create zaak when createWithDossier is set to false`() {
        `when`(zaakTypeLinkService.get("test")).thenReturn(ZaakTypeLink(
            ZaakTypeLinkId.newId(UUID.randomUUID()),
            "name",
            URI("http://some-url"),
            ServiceTaskHandlers(),
            false
        ))

        val event = mock(DocumentCreatedEvent::class.java)
        val documentDefinitionId = mock(DocumentDefinition.Id::class.java)
        val documentId = mock(Document.Id::class.java)
        `when`(event.definitionId()).thenReturn(documentDefinitionId)
        `when`(event.documentId()).thenReturn(documentId)
        `when`(documentDefinitionId.name()).thenReturn("test")

        documentCreatedListener.handle(event)

        verify(zaakService, never()).createZaakWithLink(documentId)
    }

    @Test
    fun `should not create zaak when link type is not found`() {
        `when`(zaakTypeLinkService.get("test")).thenReturn(null)

        val event = mock(DocumentCreatedEvent::class.java)
        val documentDefinitionId = mock(DocumentDefinition.Id::class.java)
        `when`(event.definitionId()).thenReturn(documentDefinitionId)
        `when`(documentDefinitionId.name()).thenReturn("test")

        documentCreatedListener.handle(event)

        verify(zaakService, never()).createZaakWithLink(any(Document.Id::class.java))
    }

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
}