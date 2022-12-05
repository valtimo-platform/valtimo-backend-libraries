package com.ritense.smartdocuments.client

import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.smartdocuments.connector.SmartDocumentsConnectorProperties
import com.ritense.smartdocuments.domain.SmartDocumentsRequest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.mock
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.client.WebClient

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SmartDocumentsClientTest {

    lateinit var mockDocumentenApi: MockWebServer
    lateinit var client: SmartDocumentsClient
    var temporaryResourceStorageService = mock<TemporaryResourceStorageService>()

    @BeforeAll
    fun setUp() {
        mockDocumentenApi = MockWebServer()
        mockDocumentenApi.start()

        val properties = SmartDocumentsConnectorProperties(
            url = mockDocumentenApi.url("/").toString()
        )

        client = SmartDocumentsClient(
            properties,
            WebClient.builder(),
            5,
            temporaryResourceStorageService
        )
    }

    @AfterAll
    fun tearDown() {
        mockDocumentenApi.shutdown()
    }


    // connector
    @Test
    fun `200 ok response should return FilesResponse when connector is used`() {
        val responseBody = """
            {
                "file": [
                    {
                        "filename": "test.pdf",
                        "document": {
                            "data": "Y29udGVudA=="
                        },
                        "outputFormat": "PDF"
                    }
                ]
            }
        """.trimIndent()

        mockDocumentenApi.enqueue(mockResponse(responseBody))

        val response = client.generateDocument(
            SmartDocumentsRequest(
                emptyMap(),
                SmartDocumentsRequest.SmartDocument(
                    SmartDocumentsRequest.Selection(
                        "group",
                        "template"
                    )
                )
            )
        )

        assertEquals(1, response.file.size)
        assertEquals("test.pdf", response.file[0].filename)
        assertEquals("PDF", response.file[0].outputFormat)
        assertEquals("Y29udGVudA==", response.file[0].document.data)
    }

    // connector
    @Test
    fun `401 Unauthorized response should throw exception when connector is used`() {
        val responseBody = """
            <!doctype html>
            <html lang="en">

            <head>
                <title>HTTP Status 401 – Unauthorized</title>
            </head>

            <body>
                <h1>HTTP Status 401 – Unauthorized</h1>
                <hr class="line" />
                <p><b>Type</b> Status Report</p>
                <p><b>Message</b> Bad credentials</p>
                <p><b>Description</b> The request has not been applied because it lacks valid authentication credentials for the
                    target resource.</p>
                <hr class="line" />
                <h3>Apache Tomcat/9.0.45</h3>
            </body>

            </html>
        """.trimIndent()

        mockDocumentenApi.enqueue(mockResponse(responseBody, "text/html; charset=utf-8", 401))

        val exception = assertThrows(HttpClientErrorException::class.java) {
            client.generateDocument(
                SmartDocumentsRequest(
                    emptyMap(),
                    SmartDocumentsRequest.SmartDocument(
                        SmartDocumentsRequest.Selection(
                            "group",
                            "template"
                        )
                    )
                )
            )
        }

        assertEquals(
            "401 The request has not been applied because it lacks valid authentication credentials for the target " +
                "resource. Response received from server:\n" + responseBody,
            exception.message
        )
    }

    // connector
    @Test
    fun `400 Bad Request response should throw exception when connector is used`() {
        val responseBody = """
            <!doctype html>
            <html lang="en">

            <head>
                <title>HTTP Status 400 – Bad Request</title>
            </head>

            <body>
                <h1>HTTP Status 400 – Bad Request</h1>
                <hr class="line" />
                <p><b>Type</b> Status Report</p>
                <p><b>Message</b> INVALID_XML: No valid template specified</p>
                <p><b>Description</b> The server cannot or will not process the request due to something that is perceived to be a
                    client error (e.g., malformed request syntax, invalid request message framing, or deceptive request routing).
                </p>
                <hr class="line" />
                <h3>Apache Tomcat/9.0.45</h3>
            </body>

            </html>
        """.trimIndent()

        mockDocumentenApi.enqueue(mockResponse(responseBody, "text/html; charset=utf-8", 400))

        val exception = assertThrows(HttpClientErrorException::class.java) {
            client.generateDocument(
                SmartDocumentsRequest(
                    emptyMap(),
                    SmartDocumentsRequest.SmartDocument(
                        SmartDocumentsRequest.Selection(
                            "group",
                            "template"
                        )
                    )
                )
            )
        }

        assertEquals(
            "400 The server cannot or will not process the request due to something that is perceived to be a client " +
                "error (e.g., no valid template specified, user has no privileges for the template, malformed request syntax, " +
                "invalid request message framing, or deceptive request routing). Response received from server:\n" + responseBody,
            exception.message
        )
    }

    private fun mockResponse(
        body: String,
        contentType: String = "application/json",
        responseCode: Int = 200
    ): MockResponse {
        return MockResponse()
            .setResponseCode(responseCode)
            .addHeader("Content-Type", contentType)
            .setBody(body)
    }
}