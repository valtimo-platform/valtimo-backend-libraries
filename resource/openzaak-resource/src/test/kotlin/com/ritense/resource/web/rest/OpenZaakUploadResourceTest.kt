package com.ritense.resource.web.rest

import com.nhaarman.mockitokotlin2.verify
import com.ritense.resource.domain.ResourceId
import com.ritense.resource.service.ResourceService
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.springframework.http.HttpStatus
import org.springframework.web.multipart.MultipartFile

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