package com.ritense.resource.listener

import com.ritense.resource.domain.ResourceStorageMetadata
import com.ritense.resource.domain.StorageMetadataKeys
import com.ritense.resource.event.ResourceStorageMetadataAvailableEvent
import com.ritense.resource.repository.ResourceStorageMetadataRepository
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
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
internal class MetadataAvailableEventListenerTest {

    lateinit var listener: MetadataAvailableEventListener
    lateinit var repository: ResourceStorageMetadataRepository

    @Captor
    lateinit var resourceStorageMetadataCaptor: ArgumentCaptor<ResourceStorageMetadata>

    @BeforeEach
    fun before() {
        repository = mock(ResourceStorageMetadataRepository::class.java)
        listener = MetadataAvailableEventListener(repository)
        resourceStorageMetadataCaptor = ArgumentCaptor.forClass(ResourceStorageMetadata::class.java)
    }

    @Test
    fun `should store resource metadata`() {
        val event = ResourceStorageMetadataAvailableEvent(
            source="source",
            resourceId="1234",
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
        assertEquals("/api/v1/documenten-api/c64ef7ee-c64ef7ee-056b9c4ba392/files/9ec35849-9ec35849-c9a2f77d76fa/download", lastValue.metadataValue)
    }
}