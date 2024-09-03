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

package com.ritense.catalogiapi.client

import com.ritense.catalogiapi.CatalogiApiAuthentication
import com.ritense.catalogiapi.client.CatalogiApiClient.Companion.INFORMATIEOBJECTTYPECACHE_KEY
import com.ritense.catalogiapi.domain.InformatieobjecttypeRichting
import com.ritense.catalogiapi.domain.InformatieobjecttypeVertrouwelijkheid
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.web.client.RestClient
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDate
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CatalogiApiClientTest {
    lateinit var mockApi: MockWebServer
    val cacheManager = mock<CacheManager>()

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
        assertEquals(
            "http://example.com/informatieobjecttype",
            recordedRequest.requestUrl?.queryParameter("informatieobjecttype")
        )
        assertEquals("inkomend", recordedRequest.requestUrl?.queryParameter("richting"))
        assertEquals("alles", recordedRequest.requestUrl?.queryParameter("status"))
        assertEquals("3", recordedRequest.requestUrl?.queryParameter("page"))
    }

    @Test
    fun `should send get informatieobjecttype request and parse response`() {
        val restClientBuilder = RestClient.builder()
        val client = CatalogiApiClient(restClientBuilder, cacheManager)

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
        val restClientBuilder = RestClient.builder()
        val client = CatalogiApiClient(restClientBuilder, cacheManager)

        val baseUrl = "http://example.com"
        val informatieobjecttypeUrl =
            "http://other-domain.com/informatieobjecttypen/f3974b80-b538-48c1-b82e-3a3113fc9971"

        val exception = assertThrows<IllegalArgumentException> {
            client.getInformatieobjecttype(
                TestAuthentication(),
                URI(baseUrl),
                URI(informatieobjecttypeUrl)
            )
        }
        assertEquals(
            "Requested url 'http://other-domain.com/informatieobjecttypen/" +
                "f3974b80-b538-48c1-b82e-3a3113fc9971' is not valid for baseUrl 'http://example.com'", exception.message
        )
    }

    @Test
    fun `should send get roltypen request and parse response`() {
        val restClientBuilder = RestClient.builder()
        val client = CatalogiApiClient(restClientBuilder, cacheManager)
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
        val restClientBuilder = RestClient.builder()
        val client = CatalogiApiClient(restClientBuilder, cacheManager)
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
        val restClientBuilder = RestClient.builder()
        val client = CatalogiApiClient(restClientBuilder, cacheManager)
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
        assertEquals(
            URI("https://example.com/resultaattypeomschrijvingen/id"),
            response.results[0].resultaattypeomschrijving
        )
        assertEquals("Ingetrokken", response.results[0].omschrijvingGeneriek)
        assertEquals(URI("https://example.com/resultaten/id"), response.results[0].selectielijstklasse)
        assertEquals("test", response.results[0].toelichting)
    }

    @Test
    fun `should get beluittypen request and parse response`() {
        val restClientBuilder = RestClient.builder()
        val client = CatalogiApiClient(restClientBuilder, cacheManager)
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

    @Test
    fun `prefillCache should prefill the cache`() {
        val restClientBuilder = RestClient.builder()
        val client = CatalogiApiClient(restClientBuilder, cacheManager)
        val baseUrl = mockApi.url("api").toString()
        val cache = mock<Cache>()
        whenever(cacheManager.getCache(INFORMATIEOBJECTTYPECACHE_KEY)).thenReturn(cache)

        val responseBody = """
            {
              "count": 1,
              "next": null,
              "previous": null,
              "results": [
                {
                  "url": "http://example.com",
                  "catalogus": "http://example.com",
                  "omschrijving": "string",
                  "vertrouwelijkheidaanduiding": "openbaar",
                  "beginGeldigheid": "2019-08-24",
                  "eindeGeldigheid": "2019-08-24",
                  "concept": true
                }
              ]
            }
        """.trimIndent()
        mockApi.enqueue(mockResponse(responseBody))

        client.prefillCache(TestAuthentication(), URI(baseUrl))

        // to make sure the request is cleaned up to prevent issues with other tests
        mockApi.takeRequest()

        verify(cacheManager).getCache(INFORMATIEOBJECTTYPECACHE_KEY)
        verify(cache).put(eq(URI("http://example.com")), any())
    }

    private fun sendGetZaaktypeInformatieobjecttypeRequest(
        request: ZaaktypeInformatieobjecttypeRequest
    ): RecordedRequest {
        val restClientBuilder = RestClient.builder()
        val client = CatalogiApiClient(restClientBuilder, cacheManager)

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
        assertEquals(
            "http://example.com/id",
            resultZaaktypeInformatieobjecttype.url.toString()
        )
        assertEquals("http://example.com/zaaktype", resultZaaktypeInformatieobjecttype.zaaktype.toString())
        assertEquals(
            "http://example.com/informatieobjecttype",
            resultZaaktypeInformatieobjecttype.informatieobjecttype.toString()
        )
        assertEquals(InformatieobjecttypeRichting.INKOMEND, resultZaaktypeInformatieobjecttype.richting)
        assertEquals(1, resultZaaktypeInformatieobjecttype.volgnummer)
        assertEquals("http://example.com/status", resultZaaktypeInformatieobjecttype.statustype.toString())

        return recordedRequest
    }

    @Test
    fun `should get zaaktypen request and parse response`() {
        val restClientBuilder = RestClient.builder()
        val client = CatalogiApiClient(restClientBuilder, cacheManager)
        val baseUrl = mockApi.url("api").toString()
        val responseBody = """
            {
                "count": 1,
                "next": null,
                "previous": null,
                "results": [
                    {
                        "url": "http://example.com/id",
                        "identificatie": "example-case",
                        "omschrijving": "Zaak type",
                        "omschrijvingGeneriek": "Zaaktype",
                        "vertrouwelijkheidaanduiding": "zaakvertrouwelijk",
                        "doel": "For test purposes.",
                        "aanleiding": "aanleiding",
                        "toelichting": "toelichting",
                        "indicatieInternOfExtern": "extern",
                        "handelingInitiator": "Indienen",
                        "onderwerp": "Example",
                        "handelingBehandelaar": "Case",
                        "doorlooptijd": "P1Y",
                        "servicenorm": null,
                        "opschortingEnAanhoudingMogelijk": false,
                        "verlengingMogelijk": true,
                        "verlengingstermijn": "P42D",
                        "trefwoorden": [
                            "example"
                        ],
                        "publicatieIndicatie": false,
                        "publicatietekst": "",
                        "verantwoordingsrelatie": [],
                        "productenOfDiensten": [
                            "https://github.com/valtimo-platform/valtimo-platform"
                        ],
                        "selectielijstProcestype": "https://ritense.com",
                        "referentieproces": {
                            "naam": "Example case",
                            "link": "http://ritense.com"
                        },
                        "catalogus": "http://localhost/catalogi/api/v1/catalogussen/8225508a-6840-413e-acc9-6422af120db1",
                        "statustypen": [
                            "http://localhost/catalogi/api/v1/statustypen/12345678-3f25-4716-5432-49ea8e954fd0"
                        ],
                        "resultaattypen": [],
                        "eigenschappen": [
                            "http://localhost/catalogi/api/v1/eigenschappen/12345678-b04b-424b-ab02-c4102b562633"
                        ],
                        "informatieobjecttypen": [
                            "http://localhost/catalogi/api/v1/informatieobjecttypen/12345678-be3b-4bad-9e3c-49a6219c92ad"
                        ],
                        "roltypen": [
                            "http://localhost/catalogi/api/v1/roltypen/12345678-c38d-47b8-bed5-994db88ead61"
                        ],
                        "besluittypen": [],
                        "deelzaaktypen": [],
                        "gerelateerdeZaaktypen": [],
                        "beginGeldigheid": "2021-01-01",
                        "eindeGeldigheid": null,
                        "versiedatum": "2021-01-01",
                        "concept": false
                    }
                ]
            }
        """.trimIndent()
        mockApi.enqueue(mockResponse(responseBody))

        val response = client.getZaaktypen(
            authentication = TestAuthentication(),
            baseUrl = URI(baseUrl),
            request = ZaaktypeRequest(page = 1)
        )

        // to make sure the request is cleaned up to prevent issues with other tests
        mockApi.takeRequest()
        assertEquals(1, response.results.size)
        val zaaktype = response.results.single()
        assertEquals("http://example.com/id", zaaktype.url.toString())
        assertEquals("Zaak type", zaaktype.omschrijving)
        assertEquals("Zaaktype", zaaktype.omschrijvingGeneriek)
    }

    @Test
    fun `should get eigenschappen request and parse response`() {
        val restClientBuilder = RestClient.builder()
        val client = CatalogiApiClient(restClientBuilder, cacheManager)
        val baseUrl = mockApi.url("api").toString()
        val responseBody = """
            {
                "count": 1,
                "next": null,
                "previous": null,
                "results": [
                    {
                        "url": "http://ritense.com/catalogi/api/v1/eigenschappen/724c0f92-683d-4dd8-a14b-75850dbf043d",
                        "naam": "achternaam",
                        "definitie": "achternaam",
                        "specificatie": {
                            "groep": "tekst",
                            "formaat": "tekst",
                            "lengte": "100",
                            "kardinaliteit": "1",
                            "waardenverzameling": []
                        },
                        "toelichting": "",
                        "zaaktype": "http://ritense.com/catalogi/api/v1/zaaktypen/35bbe19f-4ae8-4591-9763-273ad2675340"
                    }
                ]
            }
        """.trimIndent()
        mockApi.enqueue(mockResponse(responseBody))

        val response = client.getEigenschappen(
            authentication = TestAuthentication(),
            baseUrl = URI(baseUrl),
            request = EigenschapRequest(page = 1)
        )

        // to make sure the request is cleaned up to prevent issues with other tests
        mockApi.takeRequest()
        assertEquals(1, response.results.size)
        val zaaktype = response.results.single()
        assertEquals(
            "http://ritense.com/catalogi/api/v1/eigenschappen/724c0f92-683d-4dd8-a14b-75850dbf043d",
            zaaktype.url.toString()
        )
        assertEquals("achternaam", zaaktype.naam)
    }

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

    class TestAuthentication : CatalogiApiAuthentication {
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
