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

package com.ritense.documentenapi.bulk

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.documentenapi.DocumentenApiAuthentication
import com.ritense.resource.authorization.ResourcePermission
import com.ritense.resource.authorization.ResourcePermissionActionProvider
import com.ritense.valtimo.contract.json.MapperSingleton
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.mockito.kotlin.*
import org.springframework.web.client.RestClient
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class BulkDocumentenImportApiClientTest {

    lateinit var mockDocumentenApi: MockWebServer

    lateinit var objectMapper: ObjectMapper

    val outboxService: TestOutboxService = TestOutboxService()

    lateinit var authorizationService: AuthorizationService

    @BeforeAll
    fun setUp() {
        mockDocumentenApi = MockWebServer()
        mockDocumentenApi.start()
        objectMapper = MapperSingleton.get()
        authorizationService = mock()
        whenever(authorizationService.hasPermission<Any>(any())).thenReturn(true)
    }

    @BeforeEach
    fun beforeEach() {
        outboxService.clear()
        clearInvocations(authorizationService)
    }

    @AfterAll
    fun tearDown() {
        mockDocumentenApi.shutdown()
    }

    @Test
    fun `should create an import in the documents api`() {
        val client = getBulkClient()

        val responseBody = """
            {
              "statusUrl": "https://status/123",
              "reportUrl": "https://report/123",
              "uploadUrl": "https://upload/123"
            }
        """.trimIndent()

        mockDocumentenApi.enqueue(mockResponse(responseBody))

        val result = client.createImport(
            TestAuthentication(),
            mockDocumentenApi.url("/").toUri(),
        )

        val recordedRequest = mockDocumentenApi.takeRequest()

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("https://status/123", result.statusUrl)

        outboxService.assertEquals(
            DocumentImportCreated("https://status/123", objectMapper.readValue(responseBody))
        )

        didCheckPermission()
    }


    @Test
    fun `should get the status of a document import`() {
        val client = getBulkClient()

        val responseBody = """
            {
                "total": 100,
                "processed": 10,
                "processedSuccessfully": 10,
                "processedInvalid": 0,
                "status": "ok"
            }
        """.trimIndent()

        mockDocumentenApi.enqueue(mockResponse(responseBody))

        val result = client.getImportStatus(
            TestAuthentication(),
            "123",
            mockDocumentenApi.url("/").toUri(),
        )

        val recordedRequest = mockDocumentenApi.takeRequest()

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("ok", result.status)

        didCheckPermission()
    }

    @Test
    fun `should get the report of a document import`() {
        val client = getBulkClient()

        val responseBody = """
            uuid,identificatie,resultaat
            c97bd51b-949c-40b2-bc8f-665826b6d0bb,abc,ok
            dddbd51b-949c-40b2-bc8f-665826b6d0bb,xyz,ok
        """.trimIndent()

        mockDocumentenApi.enqueue(
            MockResponse()
                .addHeader("Content-Type", "text/csv")
                .setBody(responseBody)
        )

        val result = client.getImportReport(
            TestAuthentication(),
            "123",
            mockDocumentenApi.url("/").toUri(),
        )

        val recordedRequest = mockDocumentenApi.takeRequest()

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))

        val outputCsv = result.reader().use { it.readText() }
        assertThat(outputCsv).isEqualTo(responseBody)

        didCheckPermission()
    }


    @Test
    fun `should allow upload the CSV to bulk import`() {
        val client = getBulkClient()

        val uploadCsv = """
            uuid,identificatie
            c97bd51b-949c-40b2-bc8f-665826b6d0bb,abc
            dddbd51b-949c-40b2-bc8f-665826b6d0bb,xyz
        """.trimIndent()

        mockDocumentenApi.enqueue(
            MockResponse()
                .setResponseCode(200)
        )

        client.uploadImportCsv(
            TestAuthentication(),
            uploadCsv.byteInputStream(),
            "123",
            mockDocumentenApi.url("/").toUri(),
        )

        val recordedRequest = mockDocumentenApi.takeRequest()
        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("text/csv", recordedRequest.getHeader("Content-Type"))

        didCheckPermission()
    }

    private fun getBulkClient(): BulkDocumentenImportApiClient {
        val restClientBuilder = RestClient.builder()
        val client = BulkDocumentenImportApiClient(
            restClientBuilder, outboxService, objectMapper, authorizationService, true
        )
        return client
    }

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

    private fun didCheckPermission() {
        verify(authorizationService).requirePermission(
            EntityAuthorizationRequest(
                ResourcePermission::class.java,
                ResourcePermissionActionProvider.IMPORT,
                ResourcePermission()
            )
        )
    }


    class TestAuthentication : DocumentenApiAuthentication {
        override fun applyAuth(builder: RestClient.Builder): RestClient.Builder {
            return builder.defaultHeaders { headers ->
                headers.setBearerAuth("test")
            }
        }

        override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
            val filteredRequest = ClientRequest.from(request).headers { headers ->
                headers.setBearerAuth("test")
            }.build()
            return next.exchange(filteredRequest)
        }
    }
}
