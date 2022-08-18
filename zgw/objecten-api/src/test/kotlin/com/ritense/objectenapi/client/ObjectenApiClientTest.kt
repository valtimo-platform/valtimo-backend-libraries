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

package com.ritense.objectenapi.client

import com.ritense.objectenapi.ObjectenApiAuthentication
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
import java.time.LocalDate
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ObjectenApiClientTest {

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
    fun `should send get single object request and parse response`() {
        val webClient = WebClient.create()
        val client = ObjectenApiClient(webClient)

        val responseBody = """
            {
              "url": "http://example.com",
              "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
              "type": "http://example.com",
              "record": {
                "index": 0,
                "typeVersion": 32767,
                "data": {
                  "property1": "henk",
                  "property2": 123
                },
                "geometry": {
                  "type": "string",
                  "coordinates": [
                    0,
                    0
                  ]
                },
                "startAt": "2019-08-24",
                "endAt": "2019-08-25",
                "registrationAt": "2019-08-26",
                "correctionFor": "string",
                "correctedBy": "string2"
              }
            }
        """.trimIndent()

        mockApi.enqueue(mockResponse(responseBody))

        val objectUrl = mockApi.url("/some-object").toString()

        val result = client.getObject(
            TestAuthentication(),
            URI(objectUrl)
        )

        val recordedRequest = mockApi.takeRequest()
        val requestedUrl = recordedRequest.requestUrl

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))

        assertEquals(objectUrl, requestedUrl.toString())

        assertEquals(URI("http://example.com"), result.url)
        assertEquals(UUID.fromString("095be615-a8ad-4c33-8e9c-c7612fbf6c9f"), result.uuid)
        assertEquals(URI("http://example.com"), result.type)
        assertEquals(0, result.record.index)
        assertEquals(32767, result.record.typeVersion)
        assertEquals("henk", result.record.data["property1"])
        assertEquals(123, result.record.data["property2"])
        assertEquals(2, result.record.data.size)
        assertEquals("string", result.record.geometry.type)
        assertEquals(0, result.record.geometry.coordinates[0])
        assertEquals(0, result.record.geometry.coordinates[1])
        assertEquals(2, result.record.geometry.coordinates.size)
        assertEquals(LocalDate.of(2019, 8, 24), result.record.startAt)
        assertEquals(LocalDate.of(2019, 8, 25), result.record.endAt)
        assertEquals(LocalDate.of(2019, 8, 26), result.record.registrationAt)
        assertEquals("string", result.record.correctionFor)
        assertEquals("string2", result.record.correctedBy)
    }

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

    class TestAuthentication: ObjectenApiAuthentication {
        override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
            val filteredRequest = ClientRequest.from(request).headers { headers ->
                headers.setBearerAuth("test")
            }.build()
            return next.exchange(filteredRequest)
        }
    }
}