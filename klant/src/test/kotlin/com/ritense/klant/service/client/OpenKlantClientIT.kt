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

package com.ritense.klant.service.client

import com.jayway.jsonpath.JsonPath
import com.ritense.klant.client.OpenKlantClient
import com.ritense.klant.domain.KlantCreationRequest
import com.ritense.klant.domain.KlantSearchFilter
import com.ritense.klant.domain.SubjectIdentificatie
import com.ritense.klant.service.BaseIntegrationTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasProperty
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod

class OpenKlantClientIT : BaseIntegrationTest() {

    @Autowired
    lateinit var openKlantClient: OpenKlantClient

    lateinit var server: MockWebServer
    lateinit var baseUrl: String
    lateinit var executedRequests: MutableList<RecordedRequest>

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        executedRequests = mutableListOf()
        setupMockServer()
        server.start()
        baseUrl = server.url("/").toString()

        `when`(openKlantClientProperties.url).thenReturn(baseUrl)
        `when`(openKlantClientProperties.clientId).thenReturn("test")
        `when`(openKlantClientProperties.secret).thenReturn("TpT22_J>qy2cJj}=^a9K4EMZ/9K-ZAacP")
        `when`(openKlantClientProperties.rsin).thenReturn("051845623")
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `postKlant should create klant`() {
        val klantRequest = KlantCreationRequest(
            "bronorganisatie",
            "klantnummer",
            "websiteUrl",
            "subjectType",
            SubjectIdentificatie(
                "123"
            )
        )

        val klant = openKlantClient.postKlant(klantRequest)

        verifyRequestSent(HttpMethod.POST, "/klanten/api/v1/klanten")
        val request = findRequest(HttpMethod.POST, "/klanten/api/v1/klanten")

        assertNotNull(request)
        val bodyContent = request.body.readUtf8()

        assertEquals("bronorganisatie", JsonPath.read(bodyContent, "$.bronorganisatie"))
        assertEquals("klantnummer", JsonPath.read(bodyContent, "$.klantnummer"))
        assertEquals("websiteUrl", JsonPath.read(bodyContent, "$.websiteUrl"))
        assertEquals("subjectType", JsonPath.read(bodyContent, "$.subjectType"))
        assertEquals("123", JsonPath.read(bodyContent, "$.subjectIdentificatie.inpBsn"))
    }

    @Test
    fun `getKlant should get klant list with filter`() {
        val klant = openKlantClient.getKlant("123")

        verifyRequestSent(HttpMethod.GET, "/klanten/api/v1/klanten")
        assertEquals("http://example.com/with-id", klant?.url)
        assertEquals("user@example.com", klant?.emailadres)
        assertEquals("0123456789", klant?.telefoonnummer)
    }

    @Test
    fun `searchKlant should search klanten by properties in filter`() {
        openKlantClient.searchKlanten(KlantSearchFilter(
            bsn = "123",
            klantnummer = "456",
            page = 1
        ))

        verifyRequestSent(HttpMethod.GET, "/klanten/api/v1/klanten")
        val request = findRequest(HttpMethod.GET, "/klanten/api/v1/klanten")
        val params = extractQueryParams(request!!)

        assertThat(params, hasItem<Pair<String, String>>(
            allOf(
                hasProperty("first", `is`("subjectNatuurlijkPersoon__inpBsn")),
                hasProperty("second", `is`("123"))
            )
        ))
        assertThat(params, hasItem<Pair<String, String>>(
            allOf(
                hasProperty("first", `is`("klantnummer")),
                hasProperty("second", `is`("456"))
            )
        ))
        assertThat(params, hasItem<Pair<String, String>>(
            allOf(
                hasProperty("first", `is`("page")),
                hasProperty("second", `is`("1"))
            )
        ))
    }


    fun setupMockServer() {
        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                executedRequests.add(request)
                val path = request.path?.substringBefore('?')
                val response = when (request.method) {
                    "GET" -> when (path) {
                        "/klanten/api/v1/klanten" -> handleKlantSearchRequest()
                        else -> MockResponse().setResponseCode(404)
                    }
                    "POST" -> when (path) {
                        "/klanten/api/v1/klanten" -> handleKlantCreationRequest()
                        else -> MockResponse().setResponseCode(404)
                    }
                    else -> MockResponse().setResponseCode(404)
                }

                return response
            }
        }
        server.dispatcher = dispatcher
    }

    fun findRequest(method: HttpMethod, path: String): RecordedRequest? {
        return executedRequests
            .filter { method.matches(it.method!!) }
            .filter { it.path?.substringBefore('?').equals(path) }
            .firstOrNull()
    }

    fun verifyRequestSent(method: HttpMethod, path: String) {
        val request = findRequest(method, path)
        if (request == null){
            fail("Request with method $method and path $path was not sent")
        }
    }

    fun extractQueryParams(request: RecordedRequest): List<Pair<String, String>> {
        val queryParams = request.path!!.substringAfter('?').split('&')
        val params: List<Pair<String, String>> = queryParams.map {
            Pair(it.substringBefore('='), it.substringAfter('='))
        }
        return params
    }

    fun handleKlantSearchRequest(): MockResponse {
        val body = """
            {
                "count": 0,
                "next": "http://example.com",
                "previous": "http://example.com",
                "results": [
                    {
                        "url": "http://example.com/with-id",
                        "bronorganisatie": "string",
                        "klantnummer": "string",
                        "bedrijfsnaam": "string",
                        "functie": "string",
                        "websiteUrl": "http://example.com",
                        "voornaam": "string",
                        "voorvoegselAchternaam": "string",
                        "achternaam": "string",
                        "telefoonnummer": "0123456789",
                        "emailadres": "user@example.com",
                        "adres": {},
                        "subject": "http://example.com",
                        "subjectType": "natuurlijk_persoon"
                    }
                ]
            }
        """.trimIndent()

        return mockResponse(body)
    }

    fun handleKlantCreationRequest(): MockResponse {
        val body = """
            {
              "bronorganisatie": "string",
              "klantnummer": "string",
              "bedrijfsnaam": "string",
              "functie": "string",
              "websiteUrl": "http://example.com",
              "voornaam": "string",
              "voorvoegselAchternaam": "string",
              "achternaam": "string",
              "telefoonnummer": "string",
              "emailadres": "user@example.com",
              "adres": {
                "straatnaam": "string",
                "huisnummer": 0,
                "huisletter": "s",
                "huisnummertoevoeging": "stri",
                "postcode": "string",
                "woonplaatsnaam": "string",
                "landcode": "stri"
              },
              "subject": "http://example.com",
              "subjectType": "natuurlijk_persoon",
              "subjectIdentificatie": {
                "inpBsn": "string",
                "anpIdentificatie": "string",
                "inpANummer": "string",
                "geslachtsnaam": "string",
                "voorvoegselGeslachtsnaam": "string",
                "voorletters": "string",
                "voornamen": "string",
                "geslachtsaanduiding": "m",
                "geboortedatum": "string",
                "verblijfsadres": {
                  "aoaIdentificatie": "string",
                  "wplWoonplaatsNaam": "string",
                  "gorOpenbareRuimteNaam": "string",
                  "aoaPostcode": "string",
                  "aoaHuisnummer": 0,
                  "aoaHuisletter": "s",
                  "aoaHuisnummertoevoeging": "stri",
                  "inpLocatiebeschrijving": "string"
                },
                "subVerblijfBuitenland": {
                  "lndLandcode": "stri",
                  "lndLandnaam": "string",
                  "subAdresBuitenland1": "string",
                  "subAdresBuitenland2": "string",
                  "subAdresBuitenland3": "string"
                }
              }
            }
        """.trimIndent()

        return mockResponse(body)
    }

    fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(200)
            .setBody(body)
    }
}