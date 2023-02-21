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

package com.ritense.smartdocuments.client

import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.smartdocuments.BaseTest
import com.ritense.smartdocuments.connector.SmartDocumentsConnectorProperties
import com.ritense.smartdocuments.domain.DocumentFormatOption
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
import java.util.concurrent.TimeUnit.SECONDS

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SmartDocumentsClientTest : BaseTest() {

    lateinit var mockDocumentenApi: MockWebServer
    lateinit var client: SmartDocumentsClient
    lateinit var temporaryResourceStorageService: TemporaryResourceStorageService

    @BeforeAll
    fun setUp() {
        mockDocumentenApi = MockWebServer()
        mockDocumentenApi.start()

        val properties = SmartDocumentsConnectorProperties(
            url = mockDocumentenApi.url("/").toString()
        )

        temporaryResourceStorageService = TemporaryResourceStorageService()

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


    @Test
    fun `400 Bad Request response should throw exception when generating document stream`() {
        val error400ResponseBody = readFileAsString("/data/post-generate-document-400-error-response.html")
        mockDocumentenApi.enqueue(mockResponse(error400ResponseBody, "text/html; charset=utf-8", 400).setBodyDelay(1, SECONDS))

        val exception = assertThrows(IllegalStateException::class.java) {
            client.generateDocumentStream(
                SmartDocumentsRequest(
                    emptyMap(),
                    SmartDocumentsRequest.SmartDocument(
                        SmartDocumentsRequest.Selection("group", "template")
                    )
                ),
                DocumentFormatOption.PDF
            )
        }

        assertEquals(
            "SmartDocuments didn't generate any document. Please check the logs above for a HttpClientErrorException.",
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
