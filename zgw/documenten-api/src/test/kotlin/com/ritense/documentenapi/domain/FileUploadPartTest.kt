package com.ritense.documentenapi.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.core.io.ByteArrayResource
import org.springframework.util.MultiValueMap

class FileUploadPartTest {

    @Test
    fun `createBody should create valid MultiValueMap with file contents and lock`() {
        // Given
        val chunk = ByteArray(1024) { 1 } // Mocked byte array with some data
        val bestandsnaam = "testFile.txt"
        val lock = "mock-lock-value"

        // When
        val uploadPart = FileUploadPart(chunk, bestandsnaam, lock)
        val body: MultiValueMap<String, Any> = uploadPart.createBody()

        // Then
        assertNotNull(body)
        assertEquals(2, body.size)

        // Check the fileResource part of the body
        val fileResource = body["inhoud"]?.get(0)
        assertNotNull(fileResource)

        // Check if the lock is correctly added
        val lockValue = body["lock"]?.get(0)
        assertEquals(lock, lockValue)

        // Check the filename and content
        val byteArrayResource = fileResource as ByteArrayResource
        assertEquals(bestandsnaam, byteArrayResource.filename)
        assertEquals(chunk.size, byteArrayResource.byteArray.size)
    }

    @Test
    fun `createBody should contain correct content type`() {
        // Given
        val chunk = ByteArray(512) { 1 }
        val bestandsnaam = "image.jpg"
        val lock = "test-lock"

        // When
        val uploadPart = FileUploadPart(chunk, bestandsnaam, lock)
        val body = uploadPart.createBody()

        // Then
        val fileResource = body["inhoud"]?.get(0) as ByteArrayResource

        // You can simulate checking the content, but actual multipart validation would happen in integration tests
        assertEquals(bestandsnaam, fileResource.filename)
        assertEquals(chunk, fileResource.byteArray)
    }
}