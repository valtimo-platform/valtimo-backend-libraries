package com.ritense.resource.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class StorageMetadataKeysTest {

    @Test
    fun `getEnumFromKey returns success for valid key`() {
        val result = getEnumFromKey("documentId")

        assertTrue(result.isSuccess)
        result.onSuccess { enumConstant ->
            assertEquals(StorageMetadataKeys.DOCUMENT_ID, enumConstant)
        }
    }

    @Test
    fun `getEnumFromKey returns failure for invalid key`() {
        val result = getEnumFromKey("invalidKey")

        assertTrue(result.isFailure)
        result.onFailure { exception ->
            assertTrue(exception is IllegalArgumentException)
            assertEquals("Unknown storage metadata key: invalidKey", exception.message)
        }
    }

    @Test
    fun `getEnumFromKey returns success for another valid key`() {
        val result = getEnumFromKey("downloadUrl")

        assertTrue(result.isSuccess)
        result.onSuccess { enumConstant ->
            assertEquals(StorageMetadataKeys.DOWNLOAD_URL, enumConstant)
        }
    }
}