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

package com.ritense.notificatiesapi.client

import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.notificatiesapi.NotificatiesApiAuthentication
import com.ritense.notificatiesapi.domain.Abonnement
import com.ritense.notificatiesapi.domain.Kanaal
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.valtimo.contract.json.MapperSingleton
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.web.client.RestClient
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificatiesApiClientTest {
    lateinit var mockNotificatiesApi: MockWebServer
    lateinit var restClientBuilder: RestClient.Builder
    lateinit var client: NotificatiesApiClient

    @BeforeEach
    fun setup() {
        mockNotificatiesApi = MockWebServer()
        mockNotificatiesApi.start()
        restClientBuilder = RestClient.builder()
        client = NotificatiesApiClient(restClientBuilder)
    }

    @AfterEach
    fun tearDown() {
        mockNotificatiesApi.shutdown()
    }

    @Test
    fun `should get Abonnement in Notificaties API`() {

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

        val result = client.getAbonnement(
            authentication = TestAuthentication(),
            baseUrl = mockNotificatiesApi.url("/").toUri(),
            abonnmentId = "test-abonnement"
        )
        val recordedRequest = mockNotificatiesApi.takeRequest()

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("/abonnement/test-abonnement", recordedRequest.path)

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

        val result = client.createKanaal(
            authentication = TestAuthentication(),
            baseUrl = mockNotificatiesApi.url("/").toUri(),
            kanaal = Kanaal(naam = "Test Kanaal")
        )
        val recordedRequest = mockNotificatiesApi.takeRequest()
        val requestBody = MapperSingleton.get().readValue<Map<String, Any>>(
            recordedRequest.body.readUtf8()
        )

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("/kanaal", recordedRequest.path)

        assertNull(requestBody["url"])
        assertEquals("Test Kanaal", requestBody["naam"])
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

        val result = client.getKanalen(
            authentication = TestAuthentication(),
            baseUrl = mockNotificatiesApi.url("/").toUri(),
        )
        val recordedRequest = mockNotificatiesApi.takeRequest()

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("/kanaal", recordedRequest.path)

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

        val result = client.createAbonnement(
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
        val recordedRequest = mockNotificatiesApi.takeRequest()
        val requestBody = MapperSingleton.get().readValue<Map<String, Any>>(
            recordedRequest.body.readUtf8()
        )

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("/abonnement", recordedRequest.path)

        assertEquals("http://example.com", requestBody["url"])
        assertEquals("http://example.com/callback", requestBody["callbackUrl"])
        assertEquals("Bearer token", requestBody["auth"])
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

        client.deleteAbonnement(
            authentication = TestAuthentication(),
            baseUrl = mockNotificatiesApi.url("/").toUri(),
            abonnementId = abonnementId
        )
        val recordedRequest = mockNotificatiesApi.takeRequest()

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("/abonnement/$abonnementId", recordedRequest.path)
    }

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

    class TestAuthentication() : NotificatiesApiAuthentication {
        override val configurationId: PluginConfigurationId
            get() = PluginConfigurationId.newId()

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
