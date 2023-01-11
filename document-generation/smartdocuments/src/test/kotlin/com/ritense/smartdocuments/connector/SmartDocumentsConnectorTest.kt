package com.ritense.smartdocuments.connector

import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.ritense.smartdocuments.client.SmartDocumentsClient
import com.ritense.smartdocuments.domain.DocumentFormatOption
import com.ritense.smartdocuments.domain.FilesResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class SmartDocumentsConnectorTest {

    @Test
    fun `should throw NoSuchElementException when output format does not match the given format`() {
        val properties = mock<SmartDocumentsConnectorProperties>()
        val client = mock<SmartDocumentsClient>()

        val fileResponse = FilesResponse(
            listOf(
                FilesResponse.FileResponse(
                    "filename1.pdf",
                    FilesResponse.DocumentResponse("Y29udGVudA=="),
                    "PDF"
                ),
                FilesResponse.FileResponse(
                    "filename2.xml",
                    FilesResponse.DocumentResponse("Y29udGVudA=="),
                    "XML"
                )
            )
        )

        whenever(client.generateDocument(any())).thenReturn(fileResponse)

        val smartDocumentsConnector = SmartDocumentsConnector(properties, client)

        val exception = assertThrows(NoSuchElementException::class.java) {
            smartDocumentsConnector.generateDocument(
                "group",
                "templateName",
                emptyMap(),
                DocumentFormatOption.DOCX
            )
        }

        assertEquals("Requested document format is 'DOCX' but the available formats are '[PDF, XML]'", exception.message)
    }

    @Test
    fun `should return document content when output format matches the given format`() {

        val properties = mock<SmartDocumentsConnectorProperties>()
        val client = mock<SmartDocumentsClient>()

        val fileResponse = FilesResponse(
            listOf(
                FilesResponse.FileResponse(
                    "filename1.pdf",
                    FilesResponse.DocumentResponse("Y29udGVudA=="),
                    "PDF"
                ),
                FilesResponse.FileResponse(
                    "filename2.xml",
                    FilesResponse.DocumentResponse("Y29udGVudA=="),
                    "XML"
                )
            )
        )
        whenever(client.generateDocument(any())).thenReturn(fileResponse)

        val smartDocumentsConnector = SmartDocumentsConnector(properties, client)

        val generatedDocument = smartDocumentsConnector.generateDocument(
            "group",
            "templateName",
            emptyMap(),
            DocumentFormatOption.PDF
        )

        assertEquals("filename1.pdf", generatedDocument.name)
        assertEquals("pdf", generatedDocument.extension)
        assertEquals("application/pdf", generatedDocument.contentType)
        assertEquals("content", String(generatedDocument.asByteArray))
    }
}