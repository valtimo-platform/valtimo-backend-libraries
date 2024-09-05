/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.zakenapi.service

import com.ritense.temporaryresource.domain.ResourceStorageMetadata
import com.ritense.temporaryresource.domain.StorageMetadataKeys
import com.ritense.temporaryresource.repository.ResourceStorageMetadataRepository
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.zakenapi.BaseIntegrationTest
import com.ritense.zakenapi.event.ResourceStorageDocumentMetadataAvailableEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito.mock
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.lastValue
import org.mockito.kotlin.verify

internal class DocumentMetadataAvailableEventListenerIT : BaseIntegrationTest() {

    lateinit var listener: DocumentMetadataAvailableEventListener
    lateinit var repository: ResourceStorageMetadataRepository

    @Captor
    lateinit var resourceStorageMetadataCaptor: ArgumentCaptor<ResourceStorageMetadata>

    @BeforeEach
    fun before() {
        repository = mock(ResourceStorageMetadataRepository::class.java)
        listener = DocumentMetadataAvailableEventListener(repository)
        userManagementService = mock(UserManagementService::class.java)
        resourceStorageMetadataCaptor = ArgumentCaptor.forClass(ResourceStorageMetadata::class.java)
    }

    @Test
    fun `should store resource metadata`() {
        val event = ResourceStorageDocumentMetadataAvailableEvent(
            source = "source",
            resourceId = "1234",
            documentUrl = "http://localhost:8001/document/url",
            downloadUrl = "/api/v1/documenten-api/c64ef7ee-c64ef7ee-056b9c4ba392/files/9ec35849-9ec35849-c9a2f77d76fa/download",
            documentId = "9ec35849-9ec35849-c9a2f77d76fa"
        )
        listener.storeResourceMetadata(event)

        verify(repository, atLeast(2)).save(resourceStorageMetadataCaptor.capture())

        val firstValue = resourceStorageMetadataCaptor.firstValue
        assertNotNull(firstValue)
        assertEquals(StorageMetadataKeys.DOCUMENT_ID, firstValue.id.metadataKey)
        assertEquals("9ec35849-9ec35849-c9a2f77d76fa", firstValue.metadataValue)

        val lastValue = resourceStorageMetadataCaptor.lastValue
        assertNotNull(lastValue)
        assertEquals(StorageMetadataKeys.DOWNLOAD_URL, lastValue.id.metadataKey)
        assertEquals(
            "/api/v1/documenten-api/c64ef7ee-c64ef7ee-056b9c4ba392/files/9ec35849-9ec35849-c9a2f77d76fa/download",
            lastValue.metadataValue
        )
    }
}