package com.ritense.documentenapi.domain

import com.ritense.documentenapi.client.Bestandsdeel
import com.ritense.documentenapi.client.BestandsdelenRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.io.ByteArrayResource
import org.springframework.util.MultiValueMap
import java.io.ByteArrayInputStream
import java.io.InputStream

class FileUploadPartTest {

    @Test
    fun `createBody should return a valid MultiValueMap when all bytes are read successfully`() {
        // Arrange
        val bestandsdeel = Bestandsdeel(
            url = "https://example.com/file",
            omvang = 10,
            volgnummer = 1,
            voltooid = false,
            lock = "test-lock"
        )
        val inputStream: InputStream = ByteArrayInputStream(ByteArray(10) { 1 })
        val bestandsdelenRequest = BestandsdelenRequest(inputStream, lock = "test-lock")
        val fileUploadPart = FileUploadPart(bestandsdeel, bestandsdelenRequest, "testfile.txt")

        // Act
        val result: MultiValueMap<String, Any> = fileUploadPart.createBody()

        // Assert
        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals("test-lock", result["lock"]?.firstOrNull())
        assertEquals("testfile.txt", (result["inhoud"]?.firstOrNull() as ByteArrayResource).filename)
    }

    @Test
    fun `createBody should throw an exception when not all bytes are read`() {
        // Arrange
        val bestandsdeel = Bestandsdeel(
            url = "https://example.com/file",
            omvang = 10,
            volgnummer = 1,
            voltooid = false,
            lock = "test-lock"
        )
        val inputStream: InputStream = ByteArrayInputStream(ByteArray(5) { 1 })
        val bestandsdelenRequest = BestandsdelenRequest(inputStream, lock = "test-lock")
        val fileUploadPart = FileUploadPart(bestandsdeel, bestandsdelenRequest, "testfile.txt")

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            fileUploadPart.createBody()
        }
        assertEquals(
            "Failed to read all the bytes to upload. Expected 10 bytes, but only read 5 bytes. Check bestandsdeel: $bestandsdeel.",
            exception.message
        )
    }
}
