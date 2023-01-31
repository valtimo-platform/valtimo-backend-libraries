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

package com.ritense.objecttypenapi.client

import com.ritense.objecttypenapi.ObjecttypenApiAuthentication
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
internal class ObjecttypenApiClientTest {

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
    fun `should send get single objecttype request and parse response`() {
        val webclientBuilder = WebClient.builder()
        val client = ObjecttypenApiClient(webclientBuilder)

        val responseBody = """
            {
              "url": "http://example.com",
              "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
              "name": "string",
              "namePlural": "string",
              "description": "string",
              "dataClassification": "open",
              "maintainerOrganization": "string",
              "maintainerDepartment": "string",
              "contactPerson": "string",
              "contactEmail": "string",
              "source": "string",
              "updateFrequency": "real_time",
              "providerOrganization": "string",
              "documentationUrl": "http://example.com",
              "labels": {
                "property1": "something",
                "property2": "other"
              },
              "createdAt": "2019-08-24",
              "modifiedAt": "2019-08-24",
              "versions": [
                "http://example.com"
              ]
            }
        """.trimIndent()

        mockApi.enqueue(mockResponse(responseBody))

        val objecttypeUrl = mockApi.url("/some-object").toString()

        val result = client.getObjecttype(
            TestAuthentication(),
            URI(objecttypeUrl)
        )

        val recordedRequest = mockApi.takeRequest()
        val requestedUrl = recordedRequest.requestUrl

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))

        assertEquals(objecttypeUrl, requestedUrl.toString())

        assertEquals(URI("http://example.com"), result.url)
        assertEquals(UUID.fromString("095be615-a8ad-4c33-8e9c-c7612fbf6c9f"), result.uuid)
        assertEquals("string", result.name)
        assertEquals("string", result.namePlural)
        assertEquals("string", result.description)
        assertEquals(DataClassification.OPEN, result.dataClassification)
        assertEquals("string", result.maintainerOrganization)
        assertEquals("string", result.maintainerDepartment)
        assertEquals("string", result.contactPerson)
        assertEquals("string", result.contactEmail)
        assertEquals("string", result.source)
        assertEquals(UpdateFrequency.REAL_TIME, result.updateFrequency)
        assertEquals("string", result.providerOrganization)
        assertEquals(URI("http://example.com"), result.documentationUrl)
        assertEquals("something", result.labels?.get("property1").toString())
        assertEquals("other", result.labels?.get("property2").toString())
        assertEquals("other", result.labels?.get("property2").toString())
        assertEquals(LocalDate.of(2019, 8, 24), result.createdAt)
        assertEquals(LocalDate.of(2019, 8, 24), result.modifiedAt)
        assertEquals(listOf(URI("http://example.com")), result.versions)
    }

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

    class TestAuthentication: ObjecttypenApiAuthentication {
        override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
            val filteredRequest = ClientRequest.from(request).headers { headers ->
                headers.setBearerAuth("test")
            }.build()
            return next.exchange(filteredRequest)
        }
    }
}