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

package com.ritense.notificatiesapi.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.notificatiesapi.NotificatiesApiAuthentication
import com.ritense.notificatiesapi.domain.Abonnement
import com.ritense.notificatiesapi.domain.Kanaal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificatiesApiClientTest {
    lateinit var mockNotificatiesApi: MockWebServer
    lateinit var webClient: WebClient
    lateinit var client: NotificatiesApiClient

    @BeforeEach
    fun setup() {
        mockNotificatiesApi = MockWebServer()
        mockNotificatiesApi.start()
        webClient = WebClient.create()
        client = NotificatiesApiClient(webClient)
    }

    @AfterEach
    fun tearDown() {
        mockNotificatiesApi.shutdown()
    }

    @Test
    fun `should create Kanaal in Notificaties API`() {

        mockNotificatiesApi.enqueue(
            mockResponse(
                """
            {
              "url": "http://example.com/kanaal/test-kanaal",
              "naam": "Test Kanaal",
              "documentatieLink": "http://example.com/documentatie",
              "filters": [
                  "url",
                  "someid"
              ]
            }
        """.trimIndent()
            )
        )

        val result = runBlocking {
            client.createKanaal(
                authentication = TestAuthentication(),
                baseUrl = mockNotificatiesApi.url("/").toUri(),
                kanaal = Kanaal(naam = "Test Kanaal")
            )
        }
        val recordedRequest = mockNotificatiesApi.takeRequest()
        val requestBody = jacksonObjectMapper().readValue<Map<String, Any>>(
            recordedRequest.body.readUtf8()
        )

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("/api/v1/kanaal", recordedRequest.path)

        assertNull(requestBody["url"])
        assertEquals("Test Kanaal", requestBody.get("naam"))
        assertNull(requestBody["documentatieLink"])
        assertEquals(emptyList<String>(), requestBody["filters"])

        assertEquals("http://example.com/kanaal/test-kanaal", result.url)
        assertEquals("Test Kanaal", result.naam)
        assertEquals("http://example.com/documentatie", result.documentatieLink)
        assertEquals(listOf("url", "someid"), result.filters)

    }

    @Test
    fun `should get Kanalen from Notificaties API`() {

        mockNotificatiesApi.enqueue(
            mockResponse(
                """
            [
  {
    "url": "http://example.com",
    "naam": "string",
    "documentatieLink": "http://example.com",
    "filters": [
      "string"
    ]
  },
  {
    "url": "http://example.com",
    "naam": "objecten",
    "documentatieLink": "http://example.com",
    "filters": [
      "objecten"
    ]
  }
]
        """.trimIndent()
            )
        )

        val result = runBlocking {
            client.getKanalen(
                authentication = TestAuthentication(),
                baseUrl = mockNotificatiesApi.url("/").toUri(),
            )
        }
        val recordedRequest = mockNotificatiesApi.takeRequest()

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("/api/v1/kanaal", recordedRequest.path)

        assertEquals("http://example.com", result[0].url)
        assertEquals("string", result[0].naam)
        assertEquals("http://example.com", result[0].documentatieLink)
        assertEquals(listOf("string"), result[0].filters)

        assertEquals("http://example.com", result[1].url)
        assertEquals("objecten", result[1].naam)
        assertEquals("http://example.com", result[1].documentatieLink)
        assertEquals(listOf("objecten"), result[1].filters)

    }

    @Test
    fun `should create Abonnement in Notificaties API`() {

        mockNotificatiesApi.enqueue(
            mockResponse(
                """
                    {
                      "url": "http://example.com/abonnement/test-abonnement",
                      "callbackUrl": "http://example.com/callback",
                      "auth": "Bearer token",
                      "kanalen": [
                        {
                          "filters": {
                            "url": "http://example.com",
                            "someid": "1234"
                          },
                          "naam": "Test Kanaal"
                        }
                      ]
                    }
        """.trimIndent()
            )
        )

        val result = runBlocking {
            client.createAbonnement(
                authentication = TestAuthentication(),
                baseUrl = mockNotificatiesApi.url("/").toUri(),
                abonnement = Abonnement(
                    url = "http://example.com",
                    callbackUrl = "http://example.com/callback",
                    auth = "Bearer token",
                    kanalen = listOf(
                        Abonnement.Kanaal(
                            filters = mapOf(
                                "url" to "http://example.com",
                                "someid" to "1234"
                            ),
                            naam = "Test Kanaal"
                        )
                    )
                )
            )
        }
        val recordedRequest = mockNotificatiesApi.takeRequest()
        val requestBody = jacksonObjectMapper().readValue<Map<String, Any>>(
            recordedRequest.body.readUtf8()
        )

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("/api/v1/abonnement", recordedRequest.path)

        assertEquals("http://example.com", requestBody["url"])
        assertEquals("http://example.com/callback", requestBody.get("callbackUrl"))
        assertEquals("Bearer token", requestBody.get("auth"))
        assertEquals(
            listOf(
                mapOf(
                    "filters" to mapOf(
                        "url" to "http://example.com",
                        "someid" to "1234"
                    ),
                    "naam" to "Test Kanaal"
                )
            ), requestBody["kanalen"]
        )

        assertEquals("http://example.com/abonnement/test-abonnement", result.url)
        assertEquals("http://example.com/callback", result.callbackUrl)
        assertEquals("Bearer token", result.auth)
        assertEquals("Test Kanaal", result.kanalen[0].naam)
        assertEquals(
            mapOf(
                "url" to "http://example.com",
                "someid" to "1234"
            ), result.kanalen[0].filters
        )

    }

    @Test
    fun `should delete Abonnement from Notificaties API`() {
        val abonnementId = UUID.randomUUID().toString()

        mockNotificatiesApi.enqueue(
            response = MockResponse().setResponseCode(204)
        )

        runBlocking {
            client.deleteAbonnement(
                authentication = TestAuthentication(),
                baseUrl = mockNotificatiesApi.url("/").toUri(),
                abonnementId = abonnementId
            )
        }
        val recordedRequest = mockNotificatiesApi.takeRequest()

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("/api/v1/abonnement/$abonnementId", recordedRequest.path)
    }

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

    class TestAuthentication : NotificatiesApiAuthentication {
        override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
            val filteredRequest = ClientRequest.from(request).headers { headers ->
                headers.setBearerAuth("test")
            }.build()
            return next.exchange(filteredRequest)
        }
    }
}