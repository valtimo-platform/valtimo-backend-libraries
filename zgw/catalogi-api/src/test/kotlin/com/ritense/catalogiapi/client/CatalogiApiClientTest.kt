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

package com.ritense.catalogiapi.client

import com.ritense.catalogiapi.CatalogiApiAuthentication
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
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDate
import java.util.UUID

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
        val request = ZaaktypeInformatieobjecttypeRequest(
            zaaktype = URI("http://example.com/zaaktype"),
            informatieobjecttype = URI("http://example.com/informatieobjecttype"),
            richting = InformatieobjecttypeRichting.INKOMEND,
            status = ZaakTypePublishedStatus.ALLES,
            page = 3
        )
        val recordedRequest = sendGetZaaktypeInformatieobjecttypeRequest(request)

        assertEquals(5, recordedRequest.requestUrl?.querySize)
        assertEquals("http://example.com/zaaktype", recordedRequest.requestUrl?.queryParameter("zaaktype"))
        assertEquals("http://example.com/informatieobjecttype",
            recordedRequest.requestUrl?.queryParameter("informatieobjecttype"))
        assertEquals("inkomend", recordedRequest.requestUrl?.queryParameter("richting"))
        assertEquals("alles", recordedRequest.requestUrl?.queryParameter("status"))
        assertEquals("3", recordedRequest.requestUrl?.queryParameter("page"))
    }

    @Test
    fun `should send get informatieobjecttype request and parse response`() {
        val webClient = WebClient.create()
        val client = CatalogiApiClient(webClient)

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
        val webClient = WebClient.create()
        val client = CatalogiApiClient(webClient)

        val baseUrl = "http://example.com"
        val informatieobjecttypeUrl = "http://other-domain.com/informatieobjecttypen/f3974b80-b538-48c1-b82e-3a3113fc9971"

        val exception = assertThrows<IllegalArgumentException> {
            client.getInformatieobjecttype(
                TestAuthentication(),
                URI(baseUrl),
                URI(informatieobjecttypeUrl)
            )
        }
        assertEquals("Requested informatieobjecttypeUrl 'http://other-domain.com/informatieobjecttypen/" +
            "f3974b80-b538-48c1-b82e-3a3113fc9971' is not valid for baseUrl 'http://example.com'", exception.message)
    }

    @Test
    fun `should send get roltypen request and parse response`() {
        val webClient = WebClient.create()
        val client = CatalogiApiClient(webClient)
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

    private fun sendGetZaaktypeInformatieobjecttypeRequest(
        request: ZaaktypeInformatieobjecttypeRequest
    ): RecordedRequest {
        val webClient = WebClient.create()
        val client = CatalogiApiClient(webClient)

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
