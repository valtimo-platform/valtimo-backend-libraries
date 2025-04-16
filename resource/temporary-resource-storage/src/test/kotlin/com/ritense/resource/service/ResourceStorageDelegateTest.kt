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

package com.ritense.resource.service

import com.ritense.temporaryresource.domain.getEnumFromKey
import com.ritense.temporaryresource.repository.ResourceStorageMetadataRepository
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class ResourceStorageDelegateTest {

    private lateinit var service: TemporaryResourceStorageService
    private lateinit var repository: ResourceStorageMetadataRepository
    private lateinit var delegate: ResourceStorageDelegate

    @BeforeEach
    fun setUp() {
        repository = mock(ResourceStorageMetadataRepository::class.java)
        service = mock(TemporaryResourceStorageService::class.java)
        delegate = ResourceStorageDelegate(service)
    }

    @Test
    fun `should return metadata value`() {
        val fileId = "123456789-123456789"
        val metadataKey = "downloadUrl"
        val expectedMetadataValue = "https://example.com/download"

        // Mocking getEnumFromKey to return a valid enum for the test case
        whenever(service.getMetadataValue(fileId, metadataKey)).thenReturn(expectedMetadataValue)

        // Calling the method under test
        val actualMetadataValue = delegate.getMetadata(fileId, metadataKey)

        // Asserting that the actual metadata matches the expected metadata
        assertNotNull(actualMetadataValue)
        assertEquals(expectedMetadataValue, actualMetadataValue)
    }


    @Test
    fun `should fail gracefully for invalid metadata keys`() {
        val metadataKey = "invalidKey"
        val expectedFailure = getEnumFromKey(metadataKey)

        assertTrue(expectedFailure.isFailure)

        expectedFailure.onFailure { exception ->
            assertEquals("Unknown storage metadata key: invalidKey", exception.message)
        }
    }

    @Test
    fun `should fail gracefully for invalid file id`() {
        val fileId = "123456789-123456789"
        val metadataKey = "downloadUrl"
        val expectedException = EntityNotFoundException("Entity not found")

        // Mocking service to throw EntityNotFoundException when getMetadataValue is called
        whenever(service.getMetadataValue(fileId, metadataKey)).thenAnswer {
            throw expectedException
        }

        // This asserts that an exception of type EntityNotFoundException is thrown
        val exception = assertThrows<EntityNotFoundException> {
            delegate.getMetadata(fileId, metadataKey)
        }

        // This asserts that the exception message is as expected
        assertEquals(expectedException.message, exception.message)
    }

    @Test
    fun `should delete resource file`() {
        val fileId = "123456789-123456789"

        whenever(service.deleteResource(fileId)).thenReturn(true)

        val deleted = delegate.deleteResource(fileId)

        assertTrue(deleted)
    }

    @Test
    fun `should not throw an exception when file is not deleted`() {
        val fileId = "does-not-exist"

        whenever(service.deleteResource(fileId)).thenReturn(false)

        val deleted = delegate.deleteResource(fileId)

        assertFalse(deleted)
    }

}