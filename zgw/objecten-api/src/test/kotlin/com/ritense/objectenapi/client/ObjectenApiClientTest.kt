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

package com.ritense.objectenapi.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.objectenapi.ObjectenApiAuthentication
import com.ritense.objectenapi.event.ObjectViewed
import com.ritense.outbox.OutboxService
import com.ritense.outbox.domain.BaseEvent
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.data.domain.PageRequest
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDate
import java.util.UUID
import java.util.function.Supplier

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ObjectenApiClientTest {

    lateinit var mockApi: MockWebServer

    lateinit var objectMapper: ObjectMapper

    lateinit var outboxService: OutboxService

    @BeforeAll
    fun setUp() {
        mockApi = MockWebServer()
        mockApi.start()
        objectMapper = jacksonObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        outboxService = Mockito.mock(OutboxService::class.java)
    }

    @BeforeEach
    fun beforeEach() {
        reset(outboxService)
    }

    @AfterAll
    fun tearDown() {
        mockApi.shutdown()
    }

    @Test
    fun `should send get single object request and parse response`() {
        val webclientBuilder = WebClient.builder()
        val client = ObjectenApiClient(webclientBuilder, outboxService, objectMapper)

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
        assertEquals("henk", (result.record.data?.get("property1") as TextNode).asText())
        assertEquals(123, (result.record.data?.get("property2") as IntNode).asInt())
        assertEquals(2, result.record.data?.size())
        assertEquals("string", result.record.geometry?.type)
        assertEquals(0, result.record.geometry?.coordinates?.get(0))
        assertEquals(0, result.record.geometry?.coordinates?.get(1))
        assertEquals(2, result.record.geometry?.coordinates?.size)
        assertEquals(LocalDate.of(2019, 8, 24), result.record.startAt)
        assertEquals(LocalDate.of(2019, 8, 25), result.record.endAt)
        assertEquals(LocalDate.of(2019, 8, 26), result.record.registrationAt)
        assertEquals("string", result.record.correctionFor)
        assertEquals("string2", result.record.correctedBy)
    }

    @Test
    fun `should send outbox message on retrieving object`() {
        val webclientBuilder = WebClient.builder()
        val client = ObjectenApiClient(webclientBuilder, outboxService, objectMapper)

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

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        val objectUrl = mockApi.url("/some-object").toString()

        val result = client.getObject(
            TestAuthentication(),
            URI(objectUrl)
        )

        mockApi.takeRequest()

        verify(outboxService).send(eventCapture.capture())

        val firstEventValue = eventCapture.firstValue.get()
        val mappedFirstEventResult: ObjectWrapper = objectMapper.readValue(firstEventValue.result.toString())

        Assertions.assertThat(firstEventValue).isInstanceOf(ObjectViewed::class.java)
        Assertions.assertThat(result.url.toString()).isEqualTo(firstEventValue.resultId.toString())
        Assertions.assertThat(result.type).isEqualTo(mappedFirstEventResult.type)
    }

    @Test
    fun `should get objectslist`() {
        val webclientBuilder = WebClient.builder()
        val client = ObjectenApiClient(webclientBuilder, outboxService, objectMapper)

        val responseBody = """
            {
              "count": 2,
              "next": "next.url",
              "previous": "previous.url",
              "results": [{
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
              }]
            }
        """.trimIndent()

        mockApi.enqueue(mockResponse(responseBody))

        val objectUrl = mockApi.url("/some-object").toString()
        val objectTypesApiUrl = mockApi.url("/some-objectTypesApi").toString()

        val result = client.getObjectsByObjecttypeUrl(
            TestAuthentication(),
            URI(objectUrl),
            URI(objectTypesApiUrl),
            "typeId",
            PageRequest.of(0, 10)
        )

        val recordedRequest = mockApi.takeRequest()

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))

        assertEquals(2, result.count)
        assertEquals("next.url", result.next)
        assertEquals("previous.url", result.previous)
        assertEquals(URI("http://example.com"), result.results[0].url)
        assertEquals(UUID.fromString("095be615-a8ad-4c33-8e9c-c7612fbf6c9f"), result.results[0].uuid)
        assertEquals(URI("http://example.com"), result.results[0].type)
        assertEquals(0, result.results[0].record.index)
        assertEquals(32767, result.results[0].record.typeVersion)
        assertEquals("henk", (result.results[0].record.data?.get("property1") as TextNode).asText())
        assertEquals(123, (result.results[0].record.data?.get("property2") as IntNode).asInt())
        assertEquals(2, result.results[0].record.data?.size())
        assertEquals("string", result.results[0].record.geometry?.type)
        assertEquals(0, result.results[0].record.geometry?.coordinates?.get(0))
        assertEquals(0, result.results[0].record.geometry?.coordinates?.get(1))
        assertEquals(2, result.results[0].record.geometry?.coordinates?.size)
        assertEquals(LocalDate.of(2019, 8, 24), result.results[0].record.startAt)
        assertEquals(LocalDate.of(2019, 8, 25), result.results[0].record.endAt)
        assertEquals(LocalDate.of(2019, 8, 26), result.results[0].record.registrationAt)
        assertEquals("string", result.results[0].record.correctionFor)
        assertEquals("string2", result.results[0].record.correctedBy)
    }

    @Test
    fun `should send patch request`() {
        val webclientBuilder = WebClient.builder()
        val client = ObjectenApiClient(webclientBuilder, outboxService, objectMapper)

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
        val objectTypesApiUrl = mockApi.url("/some-objectTypesApi").toString().replace("localhost", "host")

        val result = client.objectPatch(
            TestAuthentication(),
            URI(objectUrl),
            ObjectRequest(
                URI(objectTypesApiUrl),
                ObjectRecord(
                    index = 1,
                    typeVersion = 2,
                    data = ObjectMapper().readTree("{\"test\":\"some-value\"}"),
                    startAt = LocalDate.of(2000, 1, 2)
                )
            )
        )

        val recordedRequest = mockApi.takeRequest()

        val expectedRequest = """
            {
               "type":"$objectTypesApiUrl",
               "record":{
                  "index":1,
                  "typeVersion":2,
                  "data":{
                     "test":"some-value"
                  },
                  "startAt":"2000-01-02"
               }
            }
        """.trimIndent()

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("PATCH", recordedRequest.method)
        assertEquals(objectUrl, recordedRequest.requestUrl.toString())
        JSONAssert.assertEquals(expectedRequest, recordedRequest.body.readUtf8(), false)

        assertEquals(URI("http://example.com"), result.url)
        assertEquals(UUID.fromString("095be615-a8ad-4c33-8e9c-c7612fbf6c9f"), result.uuid)
        assertEquals(URI("http://example.com"), result.type)
        assertEquals(0, result.record.index)
        assertEquals(32767, result.record.typeVersion)
        assertEquals("henk", (result.record.data?.get("property1") as TextNode).asText())
        assertEquals(123, (result.record.data?.get("property2") as IntNode).asInt())
        assertEquals(2, result.record.data?.size())
        assertEquals("string", result.record.geometry?.type)
        assertEquals(0, result.record.geometry?.coordinates?.get(0))
        assertEquals(0, result.record.geometry?.coordinates?.get(1))
        assertEquals(2, result.record.geometry?.coordinates?.size)
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

    class TestAuthentication : ObjectenApiAuthentication {
        override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
            val filteredRequest = ClientRequest.from(request).headers { headers ->
                headers.setBearerAuth("test")
            }.build()
            return next.exchange(filteredRequest)
        }
    }
}