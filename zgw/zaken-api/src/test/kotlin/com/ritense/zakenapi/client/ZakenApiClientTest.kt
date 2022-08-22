/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.zakenapi.client

import com.ritense.valtimo.contract.json.Mapper
import com.ritense.zakenapi.ZakenApiAuthentication
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ZakenApiClientTest {

    lateinit var mockApi: MockWebServer

    @BeforeAll
    fun setUp() {
        mockApi = MockWebServer()
        mockApi.start()
    }

    @AfterAll
    fun tearDown() {
        mockApi.shutdown()
    }

    @Test
    fun `should send link document request and parse response`() {
        val webClient = WebClient.create()
        val client = ZakenApiClient(webClient)

        val responseBody = """
            {
              "url": "http://example.com",
              "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
              "informatieobject": "http://example.com",
              "zaak": "http://example.com",
              "aardRelatieWeergave": "Hoort bij, omgekeerd: kent",
              "titel": "string",
              "beschrijving": "string",
              "registratiedatum": "2019-08-24T14:15:22Z"
            }
        """.trimIndent()

        mockApi.enqueue(mockResponse(responseBody))

        val result = client.linkDocument(
            TestAuthentication(),
            URI(mockApi.url("/").toString()),
            LinkDocumentRequest(
                "http://example.com",
                "http://example.com",
                "title",
                "description"
            )
        )

        val recordedRequest = mockApi.takeRequest()
        val requestString = recordedRequest.body.readUtf8()
        val parsedOutput = Mapper.INSTANCE.get().readValue(requestString, Map::class.java)

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))

        assertEquals("http://example.com", parsedOutput["informatieobject"])
        assertEquals("http://example.com", parsedOutput["zaak"])
        assertEquals("title", parsedOutput["titel"])
        assertEquals("description", parsedOutput["beschrijving"])

        assertEquals("http://example.com", result.url)
        assertEquals("http://example.com", result.informatieobject)
        assertEquals("http://example.com", result.zaak)
        assertEquals(UUID.fromString("095be615-a8ad-4c33-8e9c-c7612fbf6c9f"), result.uuid)
        assertEquals("string", result.titel)
        assertEquals("string", result.beschrijving)
        assertEquals("Hoort bij, omgekeerd: kent", result.aardRelatieWeergave)
        assertEquals(LocalDateTime.of(2019, 8, 24, 14, 15, 22), result.registratiedatum)
    }

    @Test
    fun `should send get zaakobjecten request and parse response`() {
        val webClient = WebClient.create()
        val client = ZakenApiClient(webClient)

        val responseBody = """
            {
              "count": 1,
              "next": "http://example.com",
              "previous": "http://example.com",
              "results": [
                {
                  "url": "http://example.com",
                  "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
                  "zaak": "http://example.com",
                  "object": "http://example.com",
                  "objectType": "adres",
                  "objectTypeOverige": "string",
                  "relatieomschrijving": "string"
                }
              ]
            }
        """.trimIndent()

        mockApi.enqueue(mockResponse(responseBody))

        val result = client.getZaakObjecten(
            TestAuthentication(),
            URI(mockApi.url("/").toString()),
            URI("http://example.org"),
            1
        )

        val recordedRequest = mockApi.takeRequest()
        val requestUrl = recordedRequest.requestUrl

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("http://example.org", requestUrl?.queryParameter("zaak"))
        assertEquals("1", requestUrl?.queryParameter("page"))

        assertEquals(1, result.count)
        assertEquals(URI("http://example.com"), result.next)
        assertEquals(URI("http://example.com"), result.previous)
        assertEquals(URI("http://example.com"), result.results[0].url)
        assertEquals(UUID.fromString("095be615-a8ad-4c33-8e9c-c7612fbf6c9f"), result.results[0].uuid)
        assertEquals(URI("http://example.com"), result.results[0].zaakUrl)
        assertEquals(URI("http://example.com"), result.results[0].objectUrl)
        assertEquals("adres", result.results[0].objectType)
        assertEquals("string", result.results[0].objectTypeOverige)
        assertEquals("string", result.results[0].relatieomschrijving)
    }

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

    class TestAuthentication: ZakenApiAuthentication {
        override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
            val filteredRequest = ClientRequest.from(request).headers { headers ->
                headers.setBearerAuth("test")
            }.build()
            return next.exchange(filteredRequest)
        }
    }
}