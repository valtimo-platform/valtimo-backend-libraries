package com.ritense.resource.web.rest

import com.nhaarman.mockitokotlin2.verify
import com.ritense.resource.service.ResourceService
import com.ritense.resource.web.ObjectContentDTO
import com.ritense.resource.web.ObjectUrlDTO
import com.ritense.resource.web.ResourceDTO
import java.net.URL
import java.util.Arrays
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.http.HttpStatus

internal class OpenZaakResourceTest {

    val resourceService = mock(ResourceService::class.java)
    var resourceController = OpenZaakResource(resourceService)

    @Test
    fun `get should get resource`() {
        val resourceId = UUID.randomUUID()
        val objectUrlDTO = ObjectUrlDTO(
            URL("http://localhost/some-download"),
            getTestResource()
        )

        `when`(resourceService.getResourceUrl(MockitoHelper.anyObject<UUID>())).thenReturn(objectUrlDTO)
        val responseEntity = resourceController.get(resourceId.toString())

        verify(resourceService).getResourceUrl(resourceId)
        assertEquals(HttpStatus.OK, responseEntity.statusCode)
        assertEquals(objectUrlDTO, responseEntity.body)
    }

    @Test
    fun `getContent should get document content`() {
        val resourceId = UUID.randomUUID()
        val objectContentDTO = ObjectContentDTO(
            URL("http://localhost/some-download"),
            getTestResource(),
            "content".toByteArray()
        )

        `when`(resourceService.getResourceContent(MockitoHelper.anyObject())).thenReturn(objectContentDTO)
        val responseEntity = resourceController.getContent(resourceId.toString())

        verify(resourceService).getResourceContent(resourceId)
        assertEquals(HttpStatus.OK, responseEntity.statusCode)
        assertTrue(Arrays.equals("content".toByteArray(), responseEntity.body))
    }

    @Test
    fun `getContent should map known file types to content type`() {
        val extensionsToTest = mapOf(
            "gif" to "image/gif",
            "jpg" to "image/jpeg",
            "pdf" to "application/pdf",
            "txt" to "text/plain",
        )
        extensionsToTest.forEach(this::downloadFileForType)
    }

    private fun downloadFileForType(extension: String, contentType: String) {
        val resourceId = UUID.randomUUID()
        val objectContentDTO = ObjectContentDTO(
            URL("http://localhost/some-download"),
            getTestResource(extension),
            "content".toByteArray()
        )

        `when`(resourceService.getResourceContent(MockitoHelper.anyObject())).thenReturn(objectContentDTO)
        val responseEntity = resourceController.getContent(resourceId.toString())

        assertEquals(HttpStatus.OK, responseEntity.statusCode)
        assertTrue(Arrays.equals("content".toByteArray(), responseEntity.body))
        assertEquals(contentType, responseEntity.headers.contentType.toString())
    }

    private fun getTestResource(extension: String = "txt"): ResourceDTO {
        return ResourceDTO(
            "id",
            "testfile.$extension",
            "testfile.$extension",
            extension,
            123L
        )
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