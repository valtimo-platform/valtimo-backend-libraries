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
import com.ritense.documentenapi.client.CreateDocumentRequest
import com.ritense.documentenapi.client.CreateDocumentResult
import com.ritense.documentenapi.client.DocumentStatusType
import com.ritense.documentenapi.client.DocumentenApiClient
import com.ritense.resource.domain.MetadataType
import com.ritense.resource.service.TemporaryResourceStorageService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.junit.jupiter.api.Test
import java.io.InputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class DocumentenApiPluginTest {

    @Test
    fun `should call client to store file`() {
        val client: DocumentenApiClient = mock()
        val storageService: TemporaryResourceStorageService = mock()
        val authenticationMock = mock<DocumentenApiAuthentication>()
        val executionMock = mock<DelegateExecution>()
        val fileStream = mock<InputStream>()

        whenever(executionMock.getVariable("localDocumentVariableName"))
            .thenReturn("localDocumentLocation")
        whenever(storageService.getResourceContentAsInputStream("localDocumentLocation"))
            .thenReturn(fileStream)
        whenever(storageService.getResourceMetadata("localDocumentLocation"))
            .thenReturn(mapOf(MetadataType.FILE_NAME.name to "test.ext"))
        whenever(client.storeDocument(any(), any(), any())).thenReturn(CreateDocumentResult("returnedUrl"))

        val plugin = DocumentenApiPlugin(client, storageService)
        plugin.url = "http://some-url"
        plugin.bronorganisatie = "123456789"
        plugin.authenticationPluginConfiguration = authenticationMock

        plugin.storeTemporaryDocument(
            executionMock,
            "localDocumentVariableName",
            "storedDocumentVariableName",
            "type",
            "taal",
            DocumentStatusType.IN_BEWERKING
        )

        val captor = argumentCaptor<CreateDocumentRequest>()
        verify(client).storeDocument(any(), any(), captor.capture())
        verify(executionMock).setVariable("storedDocumentVariableName", "returnedUrl")

        val request = captor.firstValue
        assertEquals("123456789", request.bronorganisatie)
        assertNotNull(request.creatiedatum)
        assertEquals("test.ext", request.titel)
        assertEquals("GZAC", request.auteur)
        assertEquals("test.ext", request.bestandsnaam)
        assertEquals("taal", request.taal)
        assertEquals(fileStream, request.inhoud)
        assertEquals("type", request.informatieobjecttype)
        assertEquals(DocumentStatusType.IN_BEWERKING, request.status)
        assertEquals(false, request.indicatieGebruiksrecht)
    }

}