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

package com.ritense.resource.listener

import com.ritense.openzaak.service.DocumentenService
import com.ritense.resource.domain.OpenZaakResource
import com.ritense.resource.domain.ResourceId
import com.ritense.resource.service.OpenZaakService
import com.ritense.valtimo.contract.document.event.DocumentRelatedFileAddedEvent
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

internal class DocumentRelatedFileAddedEventListenerTest {

    val openZaakService = mock(OpenZaakService::class.java)
    val documentenService = mock(DocumentenService::class.java)
    val listener = DocumentRelatedFileAddedEventListener(openZaakService, documentenService)

    @Test
    fun `ObjectInformatieObject should be created on DocumentRelatedFileSubmittedEvent for resource`() {

        val fileId = UUID.randomUUID()
        val documentId = UUID.randomUUID()

        val event = DocumentRelatedFileAddedEvent(
            UUID.randomUUID(),
            "origin",
            LocalDateTime.now(),
            "user",
            documentId,
            fileId,
            "filename.txt"
        )

        val resource = OpenZaakResource(
            ResourceId.existingId(UUID.randomUUID()),
            URI("http://some.resource.url"),
            "filename.txt",
            "txt",
            123L,
            LocalDateTime.now()
        )

        `when`(openZaakService.getResource(fileId)).thenReturn(resource)

        listener.handle(event)

        verify(openZaakService).getResource(fileId)
        verify(documentenService).createObjectInformatieObject(URI("http://some.resource.url"), documentId)
    }
}