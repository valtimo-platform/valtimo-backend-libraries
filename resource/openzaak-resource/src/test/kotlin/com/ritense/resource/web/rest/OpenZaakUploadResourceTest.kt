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

package com.ritense.resource.web.rest

import com.ritense.resource.domain.ResourceId
import com.ritense.resource.service.ResourceService
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals

internal class OpenZaakUploadResourceTest {

    val resourceService = mock(ResourceService::class.java)
    var resourceController = OpenZaakUploadResource(resourceService)

    @Test
    fun uploadOpenZaakFile() {
        val file = mock(MultipartFile::class.java)
        val openzaakResource = com.ritense.resource.domain.OpenZaakResource(
            ResourceId.existingId(UUID.randomUUID()),
            URI("documentApiUrl"),
            "name",
            "extension",
            123L,
            LocalDateTime.now()
        )

        `when`(file.originalFilename).thenReturn("originalFilename")
        `when`(resourceService.store(anyString(), anyString(), MockitoHelper.anyObject())).thenReturn(openzaakResource)

        val responseEntity = resourceController.uploadOpenZaakFile(file, "documentDefinitionName")

        verify(resourceService).store("documentDefinitionName", "originalFilename", file)
        assertEquals(HttpStatus.OK, responseEntity.statusCode)
        assertEquals(openzaakResource, responseEntity.body)
    }

    object MockitoHelper {
        fun <T> anyObject(): T {
            Mockito.any<T>()
            return uninitialized()
        }
        @Suppress("UNCHECKED_CAST")
        fun <T> uninitialized(): T =  null as T
    }
}