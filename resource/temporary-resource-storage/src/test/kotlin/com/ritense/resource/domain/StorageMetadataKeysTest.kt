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

package com.ritense.resource.domain

import com.ritense.temporaryresource.domain.StorageMetadataKeys
import com.ritense.temporaryresource.domain.getEnumFromKey
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