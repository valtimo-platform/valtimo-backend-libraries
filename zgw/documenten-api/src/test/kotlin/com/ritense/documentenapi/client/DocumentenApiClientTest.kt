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

package com.ritense.documentenapi.client

import com.ritense.documentenapi.DocumentenApiAuthentication
import com.ritense.zgw.Rsin
import com.ritense.zgw.domain.Vertrouwelijkheid
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterAll
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
import java.time.LocalDateTime
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DocumentenApiClientTest {

    lateinit var mockDocumentenApi: MockWebServer

    @BeforeAll
    fun setUp() {
        mockDocumentenApi = MockWebServer()
        mockDocumentenApi.start()
    }

    @AfterAll
    fun tearDown() {
        mockDocumentenApi.shutdown()
    }

    @Test
    fun `should send request and parse response`() {
        val webclientBuilder = WebClient.builder()
        val client = DocumentenApiClient(webclientBuilder)

        val responseBody = """
            {
              "url": "http://example.com",
              "identificatie": "string",
              "bronorganisatie": "string",
              "creatiedatum": "2019-08-24",
              "titel": "string",
              "vertrouwelijkheidaanduiding": "openbaar",
              "auteur": "string",
              "status": "in_bewerking",
              "formaat": "string",
              "taal": "str",
              "versie": 0,
              "beginRegistratie": "2019-08-24T14:15:22Z",
              "bestandsnaam": "string",
              "inhoud": "string",
              "bestandsomvang": 0,
              "link": "http://example.com",
              "beschrijving": "string",
              "ontvangstdatum": "2019-08-24",
              "verzenddatum": "2019-08-24",
              "indicatieGebruiksrecht": true,
              "ondertekening": {
                "soort": "analoog",
                "datum": "2019-08-24"
              },
              "integriteit": {
                "algoritme": "crc_16",
                "waarde": "string",
                "datum": "2019-08-24"
              },
              "informatieobjecttype": "http://example.com",
              "locked": true
            }
        """.trimIndent()

        mockDocumentenApi.enqueue(mockResponse(responseBody))

        val request = CreateDocumentRequest(
            auteur = "GZAC",
            bronorganisatie = "123",
            creatiedatum = LocalDate.of(2020, 5, 3),
            titel = "titel",
            bestandsnaam = "test",
            taal = "taal",
            inhoud = "test".byteInputStream(),
            informatieobjecttype = "type",
            status = DocumentStatusType.DEFINITIEF
        )

        val result = client.storeDocument(
            TestAuthentication(),
            mockDocumentenApi.url("/").toUri(),
            request
        )

        val recordedRequest = mockDocumentenApi.takeRequest()

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("http://example.com", result.url)
    }

    @Test
    fun `should send get document request and parse response`() {
        val webclientBuilder = WebClient.builder()
        val client = DocumentenApiClient(webclientBuilder)

        val responseBody = """
            {
              "url": "http://example.com/informatie-object/123",
              "identificatie": "identificatie",
              "bronorganisatie": "621248691",
              "creatiedatum": "2019-08-24",
              "titel": "titel",
              "vertrouwelijkheidaanduiding": "openbaar",
              "auteur": "auteur",
              "status": "in_bewerking",
              "formaat": "formaat",
              "taal": "nl",
              "versie": 4,
              "beginRegistratie": "2019-08-24T14:15:22Z",
              "bestandsnaam": "bestandsnaam",
              "inhoud": "http://example.com/inhoud",
              "bestandsomvang": 123,
              "link": "http://example.com/link",
              "beschrijving": "beschrijving",
              "ontvangstdatum": "2019-08-23",
              "verzenddatum": "2019-08-22",
              "indicatieGebruiksrecht": true,
              "ondertekening": {
                "soort": "analoog",
                "datum": "2019-08-21"
              },
              "integriteit": {
                "algoritme": "crc_16",
                "waarde": "waarde",
                "datum": "2019-08-20"
              },
              "informatieobjecttype": "http://example.com",
              "locked": true
            }
        """.trimIndent()

        mockDocumentenApi.enqueue(mockResponse(responseBody))

        val result = client.getInformatieObject(
            TestAuthentication(),
            mockDocumentenApi.url("/zaakobjects").toUri(),
        )

        val recordedRequest = mockDocumentenApi.takeRequest()

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals(URI("http://example.com/informatie-object/123"), result.url)
        assertEquals("identificatie", result.identificatie)
        assertEquals(Rsin("621248691"), result.bronorganisatie)
        assertEquals(LocalDate.of(2019, 8, 24), result.creatiedatum)
        assertEquals("titel", result.titel)
        assertEquals(Vertrouwelijkheid.OPENBAAR, result.vertrouwelijkheidaanduiding)
        assertEquals("auteur", result.auteur)
        assertEquals(DocumentStatusType.IN_BEWERKING, result.status)
        assertEquals("formaat", result.formaat)
        assertEquals("nl", result.taal)
        assertEquals(4, result.versie)
        assertEquals(LocalDateTime.of(2019, 8, 24, 14, 15, 22), result.beginRegistratie)
        assertEquals("bestandsnaam", result.bestandsnaam)
        assertEquals(123, result.bestandsomvang)
        assertEquals(URI("http://example.com/link"), result.link)
        assertEquals("beschrijving", result.beschrijving)
        assertEquals(LocalDate.of(2019, 8, 23), result.ontvangstdatum)
        assertEquals(LocalDate.of(2019, 8, 22), result.verzenddatum)
        assertEquals(true, result.indicatieGebruiksrecht)
    }

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

    class TestAuthentication: DocumentenApiAuthentication {
        override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
            val filteredRequest = ClientRequest.from(request).headers { headers ->
                headers.setBearerAuth("test")
            }.build()
            return next.exchange(filteredRequest)
        }
    }
}
