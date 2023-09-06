/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

import com.ritense.openzaak.service.DocumentenService
import com.ritense.resource.domain.OpenZaakResource
import com.ritense.resource.domain.ResourceId
import com.ritense.resource.repository.OpenZaakResourceRepository
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.time.LocalDateTime
import java.util.Arrays
import java.util.UUID
import javax.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.web.multipart.MultipartFile

internal class OpenZaakServiceTest {

    val documentenService = mock(DocumentenService::class.java)
    val repository = mock(OpenZaakResourceRepository::class.java)
    val request = mock(HttpServletRequest::class.java)
    val service = OpenZaakService(documentenService, repository, request)

    @Test
    fun `store uploads to documenten API and saves in database`() {
        val file = mock(MultipartFile::class.java)
        val resourceCaptor = ArgumentCaptor.forClass(OpenZaakResource::class.java)

        `when`(file.name).thenReturn("localfilename.txt")
        `when`(file.originalFilename).thenReturn("filename.txt")
        `when`(file.size).thenReturn(123L)
        `when`(file.contentType).thenReturn("text/plain")
        `when`(file.inputStream).thenReturn(mock(InputStream::class.java))

        `when`(documentenService.createEnkelvoudigInformatieObject("documentDefinitionName", file))
            .thenReturn(URI("http://documentapi.url"))
        `when`(repository.saveAndFlush(MockitoHelper.anyObject())).thenReturn(mock(OpenZaakResource::class.java))

        service.store("documentDefinitionName", "filename.txt", file)

        verify(repository).saveAndFlush(resourceCaptor.capture())
        verify(documentenService).createEnkelvoudigInformatieObject("documentDefinitionName", file)

        val resource = resourceCaptor.value

        assertNotNull(resource.resourceId)
        assertEquals("http://documentapi.url", resource.informatieObjectUrl.toString())
        assertEquals("filename.txt", resource.name)
        assertEquals("txt", resource.extension)
        assertEquals(123L, resource.sizeInBytes)
        assertNotNull(resource.createdOn)
    }

    @Test
    fun `getResourceUrl gets resource and builds download url`() {
        val resourceId = UUID.randomUUID()
        val resource = OpenZaakResource(
            ResourceId.newId(resourceId),
            URI("http://documentapi.url"),
            "name",
            "extension",
            321L,
            LocalDateTime.of(2000, 1, 1, 0, 0, 0)
        )

        `when`(repository.getById(MockitoHelper.anyObject())).thenReturn(resource)
        `when`(request.requestURL).thenReturn(StringBuffer().append("http://some.base.url/with/some/path"))

        val objectUrlDTO = service.getResourceUrl(resourceId)

        val returnedResource = objectUrlDTO.resource
        assertEquals(resourceId.toString(), returnedResource.id)
        assertEquals("name", returnedResource.name)
        assertEquals("name", returnedResource.key)
        assertEquals("extension", returnedResource.extension)
        assertEquals(321L, returnedResource.sizeInBytes)
        assertEquals(LocalDateTime.of(2000, 1, 1, 0, 0, 0), returnedResource.createdOn)
        assertEquals(URL("http://some.base.url/api/v1/resource/${resourceId}/download"), objectUrlDTO.url)
    }

    @Test
    fun getResourceContent() {
        val resourceId = UUID.randomUUID()
        val resource = OpenZaakResource(
            ResourceId.newId(resourceId),
            URI("http://documentapi.url"),
            "name",
            "extension",
            321L,
            LocalDateTime.of(2000, 1, 1, 0, 0, 0)
        )

        `when`(repository.getById(MockitoHelper.anyObject())).thenReturn(resource)
        `when`(request.requestURL).thenReturn(StringBuffer().append("http://some.base.url/with/some/path"))
        `when`(documentenService.getObjectInformatieObject(URI("http://documentapi.url")))
            .thenReturn("bytes".toByteArray())

        val objectContentDTO = service.getResourceContent(resourceId)

        val returnedResource = objectContentDTO.resource
        assertEquals(resourceId.toString(), returnedResource.id)
        assertEquals("name", returnedResource.name)
        assertEquals("name", returnedResource.key)
        assertEquals("extension", returnedResource.extension)
        assertEquals(321L, returnedResource.sizeInBytes)
        assertEquals(LocalDateTime.of(2000, 1, 1, 0, 0, 0), returnedResource.createdOn)

        assertEquals(URL("http://some.base.url/api/v1/resource/${resourceId}/download"), objectContentDTO.url)

        assertTrue("bytes".toByteArray().contentEquals(objectContentDTO.content))
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