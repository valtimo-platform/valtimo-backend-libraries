package com.ritense.resource.service

import com.ritense.resource.domain.ResourceStorageMetadata
import com.ritense.resource.domain.ResourceStorageMetadataId
import com.ritense.resource.domain.StorageMetadataKeys
import com.ritense.resource.repository.ResourceStorageMetadataRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ResourceStorageDelegateTest {

    lateinit var repository: ResourceStorageMetadataRepository
    lateinit var delegate: ResourceStorageDelegate

    @BeforeEach
    fun setUp() {
        repository = mock()
        delegate = ResourceStorageDelegate(repository)
    }

    @Test
    fun `should return metadata`() {
        val fileId = "123456789-123456789"
        val metadataKey = "downloadUrl"
        val expectedDownloadUrl = "https://example.com/download/url"
        val key = ResourceStorageMetadataId(
            fileId = fileId,
            metadataKey = StorageMetadataKeys.DOWNLOAD_URL
        )
        val metadata = ResourceStorageMetadata(
            key,
            expectedDownloadUrl
        )

        `when`(repository.getReferenceById(key)).thenReturn(metadata)

        val result = delegate.getMetadata(fileId, metadataKey)

        assertNotNull(result)
        assertEquals(expectedDownloadUrl, result)
    }

    @Test
    fun `should fail gracefully for invalid metadata keys`() {
        val fileId = "123456789-123456789"
        val metadataKey = "downloadUrl"
        val expectedDownloadUrl = "https://example.com/download/url"
        val key = ResourceStorageMetadataId(
            fileId = fileId,
            metadataKey = StorageMetadataKeys.DOWNLOAD_URL
        )
        val metadata = ResourceStorageMetadata(
            key,
            expectedDownloadUrl
        )

        `when`(repository.getReferenceById(key)).thenReturn(metadata)

        val result = delegate.getMetadata(fileId, metadataKey)

        assertNotNull(result)
        assertEquals(expectedDownloadUrl, result)
    }

    @Test
    fun `should fail gracefully for invalid file id`() {

    }
}