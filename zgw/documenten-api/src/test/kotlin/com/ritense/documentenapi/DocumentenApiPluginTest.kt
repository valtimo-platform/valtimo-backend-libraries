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

import com.ritense.documentenapi.DocumentenApiPlugin.Companion.DOCUMENT_URL_PROCESS_VAR
import com.ritense.documentenapi.DocumentenApiPlugin.Companion.RESOURCE_ID_PROCESS_VAR
import com.ritense.documentenapi.client.ConfidentialityLevel
import com.ritense.documentenapi.client.CreateDocumentRequest
import com.ritense.documentenapi.client.CreateDocumentResult
import com.ritense.documentenapi.client.DocumentStatusType
import com.ritense.documentenapi.client.DocumentenApiClient
import com.ritense.documentenapi.event.DocumentCreated
import com.ritense.resource.service.TemporaryResourceStorageService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import java.io.InputStream
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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
            ConfidentialityLevel.ZAAKVERTROUWELIJK.key,
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
        assertEquals(ConfidentialityLevel.ZAAKVERTROUWELIJK, request.vertrouwelijkheidaanduiding)
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

    @Test
    fun `should call client to store file after document upload`() {
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
            LocalDateTime.now()
        )

        whenever(executionMock.getVariable(RESOURCE_ID_PROCESS_VAR))
            .thenReturn("localDocumentLocation")
        whenever(storageService.getResourceContentAsInputStream("localDocumentLocation"))
            .thenReturn(fileStream)
        whenever(storageService.getResourceMetadata("localDocumentLocation"))
            .thenReturn(mapOf("title" to "title",
                "confidentialityLevel" to "zaakvertrouwelijk",
                "status" to "in_bewerking",
                "author" to "author",
                "language" to "taal",
                "filename" to "test.ext",
                "description" to "description",
                "receiptDate" to "2022-09-15",
                "sendDate" to "2022-09-16",
                "description" to "description",
                "informatieobjecttype" to "type"))
        whenever(client.storeDocument(any(), any(), any())).thenReturn(result)

        val plugin = DocumentenApiPlugin(client, storageService, applicationEventPublisher)
        plugin.url = URI("http://some-url")
        plugin.bronorganisatie = "123456789"
        plugin.authenticationPluginConfiguration = authenticationMock

        plugin.storeUploadedDocument(executionMock)

        val apiRequestCaptor = argumentCaptor<CreateDocumentRequest>()
        verify(client).storeDocument(any(), any(), apiRequestCaptor.capture())
        verify(executionMock).setVariable(DOCUMENT_URL_PROCESS_VAR, "returnedUrl")

        val request = apiRequestCaptor.firstValue
        assertEquals("123456789", request.bronorganisatie)
        assertNotNull(request.creatiedatum)
        assertEquals(LocalDate.of(2022, 9, 16), request.verzenddatum)
        assertEquals(LocalDate.of(2022, 9, 15), request.ontvangstdatum)
        assertEquals("title", request.titel)
        assertEquals("author", request.auteur)
        assertEquals("description", request.beschrijving)
        assertEquals("test.ext", request.bestandsnaam)
        assertEquals("taal", request.taal)
        assertEquals(fileStream, request.inhoud)
        assertEquals("type", request.informatieobjecttype)
        assertEquals(DocumentStatusType.IN_BEWERKING, request.status)
        assertEquals(false, request.indicatieGebruiksrecht)
        assertEquals(ConfidentialityLevel.ZAAKVERTROUWELIJK, request.vertrouwelijkheidaanduiding)
    }

    @Test
    fun `should call client to store file after document upload with minimal properties`() {
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
            LocalDateTime.now()
        )

        whenever(executionMock.getVariable(RESOURCE_ID_PROCESS_VAR))
            .thenReturn("localDocumentLocation")
        whenever(storageService.getResourceContentAsInputStream("localDocumentLocation"))
            .thenReturn(fileStream)
        whenever(storageService.getResourceMetadata("localDocumentLocation"))
            .thenReturn(mapOf("title" to "title",
                "status" to "in_bewerking",
                "language" to "taal",
                "filename" to "test.ext",
                "informatieobjecttype" to "type"))
        whenever(client.storeDocument(any(), any(), any())).thenReturn(result)

        val plugin = DocumentenApiPlugin(client, storageService, applicationEventPublisher)
        plugin.url = URI("http://some-url")
        plugin.bronorganisatie = "123456789"
        plugin.authenticationPluginConfiguration = authenticationMock

        plugin.storeUploadedDocument(executionMock)

        val apiRequestCaptor = argumentCaptor<CreateDocumentRequest>()
        verify(client).storeDocument(any(), any(), apiRequestCaptor.capture())
        verify(executionMock).setVariable(DOCUMENT_URL_PROCESS_VAR, "returnedUrl")

        val request = apiRequestCaptor.firstValue
        assertEquals("123456789", request.bronorganisatie)
        assertNotNull(request.creatiedatum)
        assertNull(request.verzenddatum)
        assertNull(request.ontvangstdatum)
        assertEquals("title", request.titel)
        assertNull(request.beschrijving)
        assertEquals("GZAC", request.auteur)
        assertEquals("test.ext", request.bestandsnaam)
        assertEquals("taal", request.taal)
        assertEquals(fileStream, request.inhoud)
        assertEquals("type", request.informatieobjecttype)
        assertEquals(DocumentStatusType.IN_BEWERKING, request.status)
        assertEquals(false, request.indicatieGebruiksrecht)
        assertNull(request.vertrouwelijkheidaanduiding)
    }

}
