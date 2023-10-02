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

package com.ritense.catalogiapi.client

import com.ritense.catalogiapi.CatalogiApiAuthentication
import com.ritense.catalogiapi.domain.InformatieobjecttypeRichting
import com.ritense.catalogiapi.domain.InformatieobjecttypeVertrouwelijkheid
import java.net.URI
import java.time.LocalDate
import java.util.UUID
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CatalogiApiClientTes {
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
    fun `should send get zaaktype informatieobjecttype request with no request variables and parse response`() {
        val request = ZaaktypeInformatieobjecttypeRequest()
        val recordedRequest = sendGetZaaktypeInformatieobjecttypeRequest(request)

        assertEquals(0, recordedRequest.requestUrl?.querySize)
    }

    @Test
    fun `should send get zaaktype informatieobjecttype request with request variables and parse response`() {
        val baseUrl = mockApi.url("api").toString()
        val zaakTypeUrl = "$baseUrl/zaaktypen/${UUID.randomUUID()}"
        val request = ZaaktypeInformatieobjecttypeRequest(
            zaaktype = URI(zaakTypeUrl),
            informatieobjecttype = URI("http://example.com/informatieobjecttype"),
            richting = InformatieobjecttypeRichting.INKOMEND,
            status = ZaakTypePublishedStatus.ALLES,
            page = 3
        )
        val recordedRequest = sendGetZaaktypeInformatieobjecttypeRequest(request)

        assertEquals(5, recordedRequest.requestUrl?.querySize)
        assertEquals(zaakTypeUrl, recordedRequest.requestUrl?.queryParameter("zaaktype"))
        assertEquals("http://example.com/informatieobjecttype",
            recordedRequest.requestUrl?.queryParameter("informatieobjecttype"))
        assertEquals("inkomend", recordedRequest.requestUrl?.queryParameter("richting"))
        assertEquals("alles", recordedRequest.requestUrl?.queryParameter("status"))
        assertEquals("3", recordedRequest.requestUrl?.queryParameter("page"))
    }

    @Test
    fun `should send get informatieobjecttype request and parse response`() {
        val webclientBuilder = WebClient.builder()
        val client = CatalogiApiClient(webclientBuilder)

        val responseBody = """
            {
              "url": "http://example.com/id",
              "catalogus": "http://example.com/catalogus",
              "omschrijving": "string",
              "vertrouwelijkheidaanduiding": "openbaar",
              "beginGeldigheid": "2019-08-24",
              "eindeGeldigheid": "2019-08-24",
              "concept": true
            }
        """.trimIndent()

        mockApi.enqueue(mockResponse(responseBody))

        val baseUrl = mockApi.url("api").toString()
        val informatieobjecttypeUrl = "$baseUrl/informatieobjecttypen/${UUID.randomUUID()}"

        val result = client.getInformatieobjecttype(
            TestAuthentication(),
            URI(baseUrl),
            URI(informatieobjecttypeUrl)
        )

        // to make sure the request is cleaned up to prevent issues with other tests
        mockApi.takeRequest()

        assertEquals("http://example.com/id", result.url.toString())
        assertEquals("http://example.com/catalogus", result.catalogus.toString())
        assertEquals("string", result.omschrijving)
        assertEquals(InformatieobjecttypeVertrouwelijkheid.OPENBAAR, result.vertrouwelijkheidaanduiding)
        assertEquals(LocalDate.of(2019, 8, 24), result.beginGeldigheid)
        assertEquals(LocalDate.of(2019, 8, 24), result.eindeGeldigheid)
        assertEquals(true, result.concept)
    }

    @Test
    fun `should not send get informatieobjecttype request when url and baseUrl dont match`() {
        val webclientBuilder = WebClient.builder()
        val client = CatalogiApiClient(webclientBuilder)

        val baseUrl = "http://example.com"
        val informatieobjecttypeUrl = "http://other-domain.com/informatieobjecttypen/f3974b80-b538-48c1-b82e-3a3113fc9971"

        val exception = assertThrows<IllegalArgumentException> {
            client.getInformatieobjecttype(
                TestAuthentication(),
                URI(baseUrl),
                URI(informatieobjecttypeUrl)
            )
        }
        assertEquals("Requested url 'http://other-domain.com/informatieobjecttypen/" +
            "f3974b80-b538-48c1-b82e-3a3113fc9971' is not valid for baseUrl 'http://example.com'", exception.message)
    }

    @Test
    fun `should send get roltypen request and parse response`() {
        val webclientBuilder = WebClient.builder()
        val client = CatalogiApiClient(webclientBuilder)
        val baseUrl = mockApi.url("api").toString()
        val zaakTypeUrl = "$baseUrl/zaaktypen/${UUID.randomUUID()}"
        val responseBody = """
            {
                "count": 1,
                "next": null,
                "previous": null,
                "results": [
                    {
                        "url": "http://example.com/id",
                        "zaaktype": "$zaakTypeUrl",
                        "omschrijving": "Aanvrager",
                        "omschrijvingGeneriek": "initiator"
                    }
                ]
            }
        """.trimIndent()
        mockApi.enqueue(mockResponse(responseBody))

        val response = client.getRoltypen(
            authentication = TestAuthentication(),
            baseUrl = URI(baseUrl),
            request = RoltypeRequest(
                zaaktype = URI(zaakTypeUrl),
                omschrijvingGeneriek = "initiator",
                status = ZaakTypePublishedStatus.ALLES,
                page = 1
            )
        )

        // to make sure the request is cleaned up to prevent issues with other tests
        mockApi.takeRequest()
        assertEquals(1, response.results.size)
        assertEquals("http://example.com/id", response.results[0].url.toString())
        assertEquals(zaakTypeUrl, response.results[0].zaaktype.toString())
        assertEquals("Aanvrager", response.results[0].omschrijving)
        assertEquals("initiator", response.results[0].omschrijvingGeneriek)
    }

    @Test
    fun `should get statustypen request and parse response`() {
        val webclientBuilder = WebClient.builder()
        val client = CatalogiApiClient(webclientBuilder)
        val baseUrl = mockApi.url("api").toString()
        val zaakTypeUrl = "$baseUrl/zaaktypen/${UUID.randomUUID()}"
        val responseBody = """
            {
                "count": 1,
                "next": null,
                "previous": null,
                "results": [
                    {
                        "url": "http://example.com/id",
                        "zaaktype": "$zaakTypeUrl",
                        "omschrijving": "Zaak afgerond",
                        "omschrijvingGeneriek": "Zaak afgerond",
                        "statustekst": "Geachte heer/mevrouw",
                        "volgnummer": 7,
                        "isEindstatus": true,
                        "informeren": true
                    }
                ]
            }
        """.trimIndent()
        mockApi.enqueue(mockResponse(responseBody))

        val response = client.getStatustypen(
            authentication = TestAuthentication(),
            baseUrl = URI(baseUrl),
            request = StatustypeRequest(
                zaaktype = URI(zaakTypeUrl),
                status = ZaakTypePublishedStatus.ALLES,
                page = 1
            )
        )

        // to make sure the request is cleaned up to prevent issues with other tests
        mockApi.takeRequest()
        assertEquals(1, response.results.size)
        assertEquals("http://example.com/id", response.results[0].url.toString())
        assertEquals(zaakTypeUrl, response.results[0].zaaktype.toString())
        assertEquals("Zaak afgerond", response.results[0].omschrijving)
        assertEquals("Zaak afgerond", response.results[0].omschrijvingGeneriek)
        assertEquals("Geachte heer/mevrouw", response.results[0].statustekst)
        assertEquals(7, response.results[0].volgnummer)
        assertEquals(true, response.results[0].isEindstatus)
        assertEquals(true, response.results[0].informeren)
    }

    @Test
    fun `should get resultaattypen request and parse response`() {
        val webclientBuilder = WebClient.builder()
        val client = CatalogiApiClient(webclientBuilder)
        val baseUrl = mockApi.url("api").toString()
        val zaakTypeUrl = "$baseUrl/zaaktypen/${UUID.randomUUID()}"
        val responseBody = """
            {
                "count": 1,
                "next": null,
                "previous": null,
                "results": [
                    {
                        "url": "http://example.com/id",
                        "zaaktype": "$zaakTypeUrl",
                        "omschrijving": "Beëindigd",
                        "resultaattypeomschrijving": "https://example.com/resultaattypeomschrijvingen/id",
                        "omschrijvingGeneriek": "Ingetrokken",
                        "selectielijstklasse": "https://example.com/resultaten/id",
                        "toelichting": "test",
                        "archiefnominatie": "vernietigen",
                        "archiefactietermijn": "P10Y",
                        "brondatumArchiefprocedure": {
                            "afleidingswijze": "afgehandeld",
                            "datumkenmerk": "",
                            "einddatumBekend": false,
                            "objecttype": "",
                            "registratie": "",
                            "procestermijn": null
                        }
                    }
                ]
            }
        """.trimIndent()
        mockApi.enqueue(mockResponse(responseBody))

        val response = client.getResultaattypen(
            authentication = TestAuthentication(),
            baseUrl = URI(baseUrl),
            request = ResultaattypeRequest(
                zaaktype = URI(zaakTypeUrl),
                status = ZaakTypePublishedStatus.ALLES,
                page = 1
            )
        )

        // to make sure the request is cleaned up to prevent issues with other tests
        mockApi.takeRequest()
        assertEquals(1, response.results.size)
        assertEquals("http://example.com/id", response.results[0].url.toString())
        assertEquals(zaakTypeUrl, response.results[0].zaaktype.toString())
        assertEquals("Beëindigd", response.results[0].omschrijving)
        assertEquals(URI("https://example.com/resultaattypeomschrijvingen/id"), response.results[0].resultaattypeomschrijving)
        assertEquals("Ingetrokken", response.results[0].omschrijvingGeneriek)
        assertEquals(URI("https://example.com/resultaten/id"), response.results[0].selectielijstklasse)
        assertEquals("test", response.results[0].toelichting)
    }

    @Test
    fun `should get beluittypen request and parse response`() {
        val webclientBuilder = WebClient.builder()
        val client = CatalogiApiClient(webclientBuilder)
        val baseUrl = mockApi.url("api").toString()
        val zaakTypeUrl = "$baseUrl/zaaktypen/${UUID.randomUUID()}"
        val responseBody = """
            {
                "count": 1,
                "next": null,
                "previous": null,
                "results": [
                    {
                        "url": "http://example.com/id",
                        "catalogus": "http://example.com/catalogussen/id",
                        "zaaktypen": [
                            "$zaakTypeUrl"
                        ],
                        "omschrijving": "Toegewezen",
                        "omschrijvingGeneriek": "Toegewezen",
                        "besluitcategorie": "",
                        "reactietermijn": null,
                        "publicatieIndicatie": false,
                        "publicatietekst": "",
                        "publicatietermijn": null,
                        "toelichting": "",
                        "informatieobjecttypen": [],
                        "beginGeldigheid": "2022-09-12",
                        "eindeGeldigheid": null,
                        "concept": false
                    }
                ]
            }
        """.trimIndent()
        mockApi.enqueue(mockResponse(responseBody))

        val response = client.getBesluittypen(
            authentication = TestAuthentication(),
            baseUrl = URI(baseUrl),
            request = BesluittypeRequest(
                zaaktypen = URI(zaakTypeUrl),
                status = ZaakTypePublishedStatus.ALLES,
                page = 1
            )
        )

        // to make sure the request is cleaned up to prevent issues with other tests
        mockApi.takeRequest()
        assertEquals(1, response.results.size)
        assertEquals("http://example.com/id", response.results[0].url.toString())
        assertEquals("http://example.com/catalogussen/id", response.results[0].catalogus.toString())
        assertEquals(zaakTypeUrl, response.results[0].zaaktypen[0].toString())
        assertEquals("Toegewezen", response.results[0].omschrijving)
        assertEquals("Toegewezen", response.results[0].omschrijvingGeneriek)
        assertEquals(false, response.results[0].publicatieIndicatie)
        assertEquals(LocalDate.parse("2022-09-12"), response.results[0].beginGeldigheid)
    }

    private fun sendGetZaaktypeInformatieobjecttypeRequest(
        request: ZaaktypeInformatieobjecttypeRequest
    ): RecordedRequest {
        val webclientBuilder = WebClient.builder()
        val client = CatalogiApiClient(webclientBuilder)

        val responseBody = """
            {
              "count": 0,
              "next": "http://example.com/next",
              "previous": "http://example.com/previous",
              "results": [
                {
                  "url": "http://example.com/id",
                  "zaaktype": "http://example.com/zaaktype",
                  "informatieobjecttype": "http://example.com/informatieobjecttype",
                  "volgnummer": 1,
                  "richting": "inkomend",
                  "statustype": "http://example.com/status"
                }
              ]
            }
        """.trimIndent()

        mockApi.enqueue(mockResponse(responseBody))

        val baseUrl = mockApi.url("api").toString()

        val result = client.getZaaktypeInformatieobjecttypes(
            TestAuthentication(),
            URI(baseUrl),
            request
        )

        val recordedRequest = mockApi.takeRequest()
        val requestedUrl = recordedRequest.requestUrl

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))

        assertEquals("$baseUrl/zaaktype-informatieobjecttypen", requestedUrl.toString().substringBefore("?"))

        assertEquals("http://example.com/next", result.next.toString())
        assertEquals("http://example.com/previous", result.previous.toString())
        assertEquals(0, result.count)
        assertEquals(1, result.results.size)

        val resultZaaktypeInformatieobjecttype = result.results[0]
        assertEquals("http://example.com/id",
            resultZaaktypeInformatieobjecttype.url.toString())
        assertEquals("http://example.com/zaaktype", resultZaaktypeInformatieobjecttype.zaaktype.toString())
        assertEquals("http://example.com/informatieobjecttype",
            resultZaaktypeInformatieobjecttype.informatieobjecttype.toString())
        assertEquals(InformatieobjecttypeRichting.INKOMEND, resultZaaktypeInformatieobjecttype.richting)
        assertEquals(1, resultZaaktypeInformatieobjecttype.volgnummer)
        assertEquals("http://example.com/status", resultZaaktypeInformatieobjecttype.statustype.toString())

        return recordedRequest
    }

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

    class TestAuthentication: CatalogiApiAuthentication {
        override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
            val filteredRequest = ClientRequest.from(request).headers { headers ->
                headers.setBearerAuth("test")
            }.build()
            return next.exchange(filteredRequest)
        }
    }
}
