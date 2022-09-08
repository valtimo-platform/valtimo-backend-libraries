/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.documentenapi

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.documentenapi.client.ConfidentialityNotice
import com.ritense.documentenapi.client.CreateDocumentRequest
import com.ritense.documentenapi.client.CreateDocumentResult
import com.ritense.documentenapi.client.DocumentStatusType
import com.ritense.documentenapi.client.DocumentenApiClient
import com.ritense.documentenapi.event.DocumentCreated
import com.ritense.resource.service.TemporaryResourceStorageService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import java.io.InputStream
import java.net.URI
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class DocumentenApiPluginTest {

    @Test
    fun `should call client to store file`() {
        val client: DocumentenApiClient = mock()
        val storageService: TemporaryResourceStorageService = mock()
        val applicationEventPublisher: ApplicationEventPublisher= mock()
        val authenticationMock = mock<DocumentenApiAuthentication>()
        val executionMock = mock<DelegateExecution>()
        val fileStream = mock<InputStream>()
        val result = CreateDocumentResult(
            "returnedUrl",
            "returnedAuthor",
            "returnedFileName",
            1L,
            LocalDateTime.of(2020, 1, 1, 1, 1, 1)
        )

        whenever(executionMock.getVariable("localDocumentVariableName"))
            .thenReturn("localDocumentLocation")
        whenever(storageService.getResourceContentAsInputStream("localDocumentLocation"))
            .thenReturn(fileStream)
        whenever(client.storeDocument(any(), any(), any())).thenReturn(result)

        val plugin = DocumentenApiPlugin(client, storageService, applicationEventPublisher)
        plugin.url = URI("http://some-url")
        plugin.bronorganisatie = "123456789"
        plugin.authenticationPluginConfiguration = authenticationMock

        plugin.storeTemporaryDocument(
            executionMock,
            "test.ext",
            ConfidentialityNotice.ZAAKVERTROUWELIJK.key,
            "title",
            "description",
            "localDocumentVariableName",
            "storedDocumentVariableName",
            "type",
            "taal",
            DocumentStatusType.IN_BEWERKING
        )

        val apiRequestCaptor = argumentCaptor<CreateDocumentRequest>()
        val eventCaptor = argumentCaptor<DocumentCreated>()
        verify(client).storeDocument(any(), any(), apiRequestCaptor.capture())
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture())
        verify(executionMock).setVariable("storedDocumentVariableName", "returnedUrl")

        val request = apiRequestCaptor.firstValue
        assertEquals("123456789", request.bronorganisatie)
        assertNotNull(request.creatiedatum)
        assertEquals("title", request.titel)
        assertEquals("GZAC", request.auteur)
        assertEquals("test.ext", request.bestandsnaam)
        assertEquals("taal", request.taal)
        assertEquals(fileStream, request.inhoud)
        assertEquals("type", request.informatieobjecttype)
        assertEquals(DocumentStatusType.IN_BEWERKING, request.status)
        assertEquals(false, request.indicatieGebruiksrecht)

        val emittedEvent = eventCaptor.firstValue
        assertEquals("returnedUrl", emittedEvent.url)
        assertEquals("returnedAuthor", emittedEvent.auteur)
        assertEquals("returnedFileName", emittedEvent.bestandsnaam)
        assertEquals(1L, emittedEvent.bestandsomvang)
        assertEquals(LocalDateTime.of(2020, 1, 1, 1, 1, 1), emittedEvent.beginRegistratie)
    }

}
