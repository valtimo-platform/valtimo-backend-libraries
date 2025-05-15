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

package com.ritense.smartdocuments.client

import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.smartdocuments.BaseTest
import com.ritense.smartdocuments.connector.SmartDocumentsConnectorProperties
import com.ritense.smartdocuments.domain.DocumentFormatOption
import com.ritense.smartdocuments.domain.SmartDocumentsRequest
import com.ritense.smartdocuments.domain.SmartDocumentsTemplateData
import com.ritense.smartdocuments.dto.SmartDocumentsPropertiesDto
import com.ritense.temporaryresource.repository.ResourceStorageMetadataRepository
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.valtimo.contract.upload.ValtimoUploadProperties
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SmartDocumentsClientTest : BaseTest() {

    private lateinit var mockDocumentenApi: MockWebServer
    private lateinit var client: SmartDocumentsClient
    private lateinit var temporaryResourceStorageService: TemporaryResourceStorageService
    private lateinit var repository: ResourceStorageMetadataRepository

    @BeforeAll
    fun setUp() {
        mockDocumentenApi = MockWebServer()
        mockDocumentenApi.start()

        repository = mock()
        val properties = SmartDocumentsConnectorProperties(
            url = mockDocumentenApi.url("/").toString()
        )

        temporaryResourceStorageService = spy(
            TemporaryResourceStorageService(
                uploadProperties = ValtimoUploadProperties(),
                objectMapper = MapperSingleton.get(),
                repository = repository
            )
        )

        client = spy(
            SmartDocumentsClient(
                properties,
                RestClient.builder(),
                5,
                temporaryResourceStorageService,
            )
        )
    }

    @BeforeEach
    fun resetMocks() {
        Mockito.reset(temporaryResourceStorageService, client)
    }

    @AfterAll
    fun tearDown() {
        mockDocumentenApi.shutdown()
    }

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
        assertThat(exception.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(exception.message).containsIgnoringCase("HTTP Status 401 – Unauthorized")
    }

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
        assertThat(exception.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `200 ok response should return FilesResponse when generating document stream`() {
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

        val documentResult = client.generateDocumentStream(
            SmartDocumentsRequest(
                emptyMap(),
                SmartDocumentsRequest.SmartDocument(
                    SmartDocumentsRequest.Selection("group", "template")
                )
            ),
            DocumentFormatOption.PDF
        )

        assertThat(documentResult.documentData.available()).isGreaterThan(0)
        assertThat(documentResult.filename).isEqualTo("test.pdf")
        assertThat(documentResult.extension).isEqualTo("pdf")
        verify(temporaryResourceStorageService, times(1)).store(any(), any())
    }

    @Test
    fun `400 Bad Request response should throw exception when generating document stream`() {
        val responseBody = readFileAsString("/data/post-generate-document-400-error-response.html")
        mockDocumentenApi.enqueue(mockResponse(responseBody, "text/html; charset=utf-8", 400))

        val exception = assertThrows(HttpClientErrorException::class.java) {
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

        assertThat(exception.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        verify(temporaryResourceStorageService, never()).store(any(), any())
    }

    @Test
    fun `200 ok response should return DocumentStructure`() {
        // given
        mockDocumentenApi.enqueue(
            mockResponse(
                body = smartDocumentsTemplateXml(),
                contentType = "application/xml"
            )
        )

        // when
        val response = client.getSmartDocumentsTemplateData(
            SmartDocumentsPropertiesDto(
                username = "username",
                password = "password",
                url = mockDocumentenApi.url("").toString()
            )
        )

        // then
        assertThat(response).isNotNull
        assertThat(response).isInstanceOf(SmartDocumentsTemplateData::class.java)
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

    private fun smartDocumentsTemplateXml() =
        """
<SmartDocuments>
    <DocumentsStructure>
        <TemplatesStructure IsAccessible="true">
            <TemplateGroups>
                <TemplateGroup IsAccessible="true" ID="34" Name="Werkzaamheden">
                    <TemplateGroups>
                        <TemplateGroup IsAccessible="true" ID="34" Name="AI">
                            <TemplateGroups/>
                            <Templates>
                                <Template ID="3523" Name="Bla"/>
                                <Template ID="223" Name="Plan intakegesprek"/>
                                <!-- More templates here... -->
                            </Templates>
                        </TemplateGroup>
                        <TemplateGroup IsAccessible="true" ID="F6F9A5AE24834A2AA9612894506AC681" Name="ANW">
                            <TemplateGroups/>
                            <Templates>
                                <Template ID="234" Name="Plan intakegesprek"/>
                                <Template ID="43" Name="Plan intakegesprek"/>
                            </Templates>
                        </TemplateGroup>
                    </TemplateGroups>
                    <Templates>
                        <Template ID="343" Name="Voorbeeld sjabloon"/>
                        <Template ID="43" Name="Voorbeeld sjabloon 2"/>
                    </Templates>
                </TemplateGroup>
            </TemplateGroups>
        </TemplatesStructure>
    </DocumentsStructure>
    <UsersStructure IsAccessible="true">
        <GroupsAccess>
            <TemplateGroups/>
            <HeaderGroups/>
        </GroupsAccess>
        <UserGroups>
            <UserGroup IsAccessible="true" ID="342" Name="Test">
                <GroupsAccess>
                    <TemplateGroups>
                        <TemplateGroup ID="343" Name="Test" AllDescendants="true"/>
                        <TemplateGroup ID="324" Name="Werkzaamheden" AllDescendants="true"/>
                    </TemplateGroups>
                    <HeaderGroups/>
                </GroupsAccess>
            </UserGroup>
        </UserGroups>
    </UsersStructure>
</SmartDocuments>
        """.trimIndent()
}