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

package com.ritense.documentenapi.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.documentenapi.DocumentenApiAuthentication
import com.ritense.documentenapi.event.DocumentDeleted
import com.ritense.documentenapi.event.DocumentInformatieObjectDownloaded
import com.ritense.documentenapi.event.DocumentInformatieObjectViewed
import com.ritense.documentenapi.event.DocumentStored
import com.ritense.documentenapi.event.DocumentUpdated
import com.ritense.outbox.OutboxService
import com.ritense.outbox.domain.BaseEvent
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.zgw.Rsin
import com.ritense.zgw.domain.Vertrouwelijkheid
import com.ritense.zgw.exceptions.RequestFailedException
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.function.Supplier
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DocumentenApiClientTest {

    lateinit var mockDocumentenApi: MockWebServer

    lateinit var objectMapper: ObjectMapper

    lateinit var outboxService: OutboxService


    @BeforeAll
    fun setUp() {
        mockDocumentenApi = MockWebServer()
        mockDocumentenApi.start()
        objectMapper = MapperSingleton.get()
        outboxService = Mockito.mock(OutboxService::class.java)
    }

    @BeforeEach
    fun beforeEach() {
        reset(outboxService)
    }

    @AfterAll
    fun tearDown() {
        mockDocumentenApi.shutdown()
    }

    @Test
    fun `should send request and parse response`() {
        val webclientBuilder = WebClient.builder()
        val client = DocumentenApiClient(webclientBuilder, outboxService, objectMapper, mock())

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
    fun `should send outbox message on saving document`() {
        val webclientBuilder = WebClient.builder()
        val client = DocumentenApiClient(webclientBuilder, outboxService, objectMapper, mock())
        val documentURL = "http://example.com"

        val responseBody = """
            {
              "url": "$documentURL",
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

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        val result = client.storeDocument(
            TestAuthentication(),
            mockDocumentenApi.url("/").toUri(),
            request
        )

        mockDocumentenApi.takeRequest()

        verify(outboxService).send(eventCapture.capture())

        val firstEventValue = eventCapture.firstValue.get()
        val mappedFirstEventResult: CreateDocumentResult = objectMapper.readValue(firstEventValue.result.toString())

        Assertions.assertThat(firstEventValue).isInstanceOf(DocumentStored::class.java)
        Assertions.assertThat(firstEventValue.resultId.toString()).isEqualTo(documentURL)
        Assertions.assertThat(mappedFirstEventResult.auteur).isEqualTo(result.auteur)
    }

    @Test
    fun `should not send outbox message on error when saving document`() {
        val webclientBuilder = WebClient.builder()
        val client = DocumentenApiClient(webclientBuilder, outboxService, objectMapper, mock())

        mockDocumentenApi.enqueue(mockResponse("").setResponseCode(400))

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

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        try {
            client.storeDocument(
                TestAuthentication(),
                mockDocumentenApi.url("/").toUri(),
                request
            )
        } catch (_: RequestFailedException) {
        }

        mockDocumentenApi.takeRequest()

        verify(outboxService, times(0)).send(eventCapture.capture())
    }

    @Test
    fun `should send get document request and parse response`() {
        val webclientBuilder = WebClient.builder()
        val client = DocumentenApiClient(webclientBuilder, outboxService, objectMapper, mock())

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

    @Test
    fun `should send outbox message on retrieving document informatieobject`() {
        val webclientBuilder = WebClient.builder()
        val client = DocumentenApiClient(webclientBuilder, outboxService, objectMapper, mock())
        val documentInformatieObjectUrl = "http://example.com/informatie-object/123"
        val responseBody = """
            {
              "url": "$documentInformatieObjectUrl",
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

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        val result = client.getInformatieObject(
            TestAuthentication(),
            mockDocumentenApi.url("/zaakobjects").toUri(),
        )

        mockDocumentenApi.takeRequest()

        verify(outboxService).send(eventCapture.capture())
        val firstEventValue = eventCapture.firstValue.get()
        val mappedFirstEventResult: DocumentInformatieObject = objectMapper.readValue(firstEventValue.result.toString())

        Assertions.assertThat(firstEventValue).isInstanceOf(DocumentInformatieObjectViewed::class.java)
        Assertions.assertThat(firstEventValue.resultId).isEqualTo(documentInformatieObjectUrl)
        Assertions.assertThat(mappedFirstEventResult.auteur).isEqualTo(result.auteur)
    }

    @Test
    fun `should not send outbox message on error retrieving document informatieobject`() {
        val webclientBuilder = WebClient.builder()
        val client = DocumentenApiClient(webclientBuilder, outboxService, objectMapper, mock())

        mockDocumentenApi.enqueue(mockResponse("").setResponseCode(400))

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        try {
            client.getInformatieObject(
                TestAuthentication(),
                mockDocumentenApi.url("/zaakobjects").toUri(),
            )
        } catch (_: RequestFailedException) {
        }

        mockDocumentenApi.takeRequest()

        verify(outboxService, times(0)).send(eventCapture.capture())
    }

    @Test
    fun `should send outbox message on download document informatieobject content`() {
        val webclientBuilder = WebClient.builder()
        val client = DocumentenApiClient(webclientBuilder, outboxService, objectMapper, mock())
        val documentInformatieObjectId = "123"
        val buffer = Buffer()

        buffer.writeUtf8("test")

        mockDocumentenApi.enqueue(mockInputStreamResponse(buffer))

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        client.downloadInformatieObjectContent(
            TestAuthentication(),
            mockDocumentenApi.url("/").toUri(),
            documentInformatieObjectId
        )

        mockDocumentenApi.takeRequest()

        Thread.sleep(1000)

        verify(outboxService).send(eventCapture.capture())

        val firstEventValue = eventCapture.firstValue.get()

        Assertions.assertThat(firstEventValue).isInstanceOf(DocumentInformatieObjectDownloaded::class.java)
        Assertions.assertThat(firstEventValue.resultId).contains(documentInformatieObjectId)
    }

    @Test
    fun `should not send outbox message on error download document informatieobject content`() {
        val webclientBuilder = WebClient.builder()
        val client = DocumentenApiClient(webclientBuilder, outboxService, objectMapper, mock())
        val documentInformatieObjectId = "123"

        mockDocumentenApi.enqueue(mockResponse("").setResponseCode(400))

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        try {
            client.downloadInformatieObjectContent(
                TestAuthentication(),
                mockDocumentenApi.url("/").toUri(),
                documentInformatieObjectId
            )
        } catch (_: WebClientResponseException) {
        }

        mockDocumentenApi.takeRequest()

        verify(outboxService, times(0)).send(eventCapture.capture())
    }

    @Test
    fun `should send delete document request and send event`() {
        val webclientBuilder = WebClient.builder()
        val client = DocumentenApiClient(webclientBuilder, outboxService, objectMapper, mock())
        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        mockDocumentenApi.enqueue(MockResponse().setResponseCode(204))

        client.deleteInformatieObject(
            TestAuthentication(),
            mockDocumentenApi.url("/documenten/api/v1/enkelvoudiginformatieobjecten/123").toUri(),
        )

        val recordedRequest = mockDocumentenApi.takeRequest()

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("/documenten/api/v1/enkelvoudiginformatieobjecten/123", recordedRequest.path)
        assertEquals("DELETE", recordedRequest.method)

        verify(outboxService, times(1)).send(eventCapture.capture())
        assertIs<DocumentDeleted>(eventCapture.firstValue.get())
        val deleteEvent = eventCapture.firstValue.get() as DocumentDeleted
        assertTrue(deleteEvent.resultId.toString().endsWith("documenten/api/v1/enkelvoudiginformatieobjecten/123"))
        assertEquals("com.ritense.documentenapi.client.DocumentInformatieObject", deleteEvent.resultType)
        assertEquals("com.ritense.gzac.drc.document.deleted", deleteEvent.type)
        assertEquals(null, deleteEvent.result)
    }

    @Test
    fun `should not send outbox message on error deleting document informatieobject`() {
        val webclientBuilder = WebClient.builder()
        val client = DocumentenApiClient(webclientBuilder, outboxService, objectMapper, mock())

        mockDocumentenApi.enqueue(mockResponse("{}").setResponseCode(400))

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        assertThrows<RequestFailedException> {
            client.deleteInformatieObject(
                TestAuthentication(),
                mockDocumentenApi.url("/zaakobjects").toUri(),
            )
        }

        mockDocumentenApi.takeRequest()

        verify(outboxService, times(0)).send(eventCapture.capture())
    }

    @Test
    fun `should send patch document object request and send event`() {
        val webclientBuilder = WebClient.builder()
        val client = DocumentenApiClient(webclientBuilder, outboxService, objectMapper, mock())
        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        val documentInformatieObjectUrl = mockDocumentenApi.url("/informatie-object/123").toUri()
        val responseBody = """
            {
              "url": "$documentInformatieObjectUrl",
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

        client.modifyInformatieObject(
            TestAuthentication(),
            documentInformatieObjectUrl,
            PatchDocumentRequest(
                creatiedatum = LocalDate.of(2020, 5, 3),
                titel = "titel",
                auteur = "auteur",
                status = DocumentStatusType.DEFINITIEF,
                taal = "taal",
                bestandsnaam = "test",
                beschrijving = "beschrijving",
                ontvangstdatum = LocalDate.of(2020, 5, 3),
                verzenddatum = LocalDate.of(2020, 5, 3),
                indicatieGebruiksrecht = true
            )
        )

        val recordedRequest = mockDocumentenApi.takeRequest()

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("/informatie-object/123", recordedRequest.path)
        assertEquals("PATCH", recordedRequest.method)

        // validate request body
        val requestBody = objectMapper.readTree(recordedRequest.body.readUtf8())
        assertEquals("2020-05-03", requestBody.get("creatiedatum").asText())
        assertEquals("titel", requestBody.get("titel").asText())
        assertEquals("auteur", requestBody.get("auteur").asText())
        assertEquals("definitief", requestBody.get("status").asText())
        assertEquals("taal", requestBody.get("taal").asText())
        assertEquals("test", requestBody.get("bestandsnaam").asText())
        assertEquals("beschrijving", requestBody.get("beschrijving").asText())
        assertEquals("2020-05-03", requestBody.get("ontvangstdatum").asText())
        assertEquals("2020-05-03", requestBody.get("verzenddatum").asText())
        assertEquals(true, requestBody.get("indicatieGebruiksrecht").asBoolean())

        //verify reqyest sent
        verify(outboxService, times(1)).send(eventCapture.capture())
        assertIs<DocumentUpdated>(eventCapture.firstValue.get())
        val event = eventCapture.firstValue.get() as DocumentUpdated
        assertTrue(event.resultId.toString().endsWith("informatie-object/123"))
        assertEquals("com.ritense.documentenapi.client.DocumentInformatieObject", event.resultType)
        assertEquals("com.ritense.gzac.drc.document.updated", event.type)
        val eventResult: DocumentInformatieObject = objectMapper.readValue(event.result.toString())
        assertEquals("auteur", eventResult.auteur)
    }

    @Test
    fun `should not send outbox message on error updating document informatieobject`() {
        val webclientBuilder = WebClient.builder()
        val client = DocumentenApiClient(webclientBuilder, outboxService, objectMapper, mock())

        mockDocumentenApi.enqueue(mockResponse("{}").setResponseCode(400))

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        assertThrows<RequestFailedException> {
            client.modifyInformatieObject(
                TestAuthentication(),
                mockDocumentenApi.url("/zaakobjects").toUri(),
                PatchDocumentRequest(
                    creatiedatum = LocalDate.of(2020, 5, 3),
                    titel = "titel",
                    auteur = "auteur",
                    status = DocumentStatusType.DEFINITIEF,
                    taal = "taal",
                    bestandsnaam = "test",
                    beschrijving = "beschrijving",
                    ontvangstdatum = LocalDate.of(2020, 5, 3),
                    verzenddatum = LocalDate.of(2020, 5, 3),
                    indicatieGebruiksrecht = true
                )
            )
        }

        mockDocumentenApi.takeRequest()

        verify(outboxService, times(0)).send(eventCapture.capture())
    }

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

    private fun mockInputStreamResponse(buffer: Buffer): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/octet-stream")
            .setBody(buffer)
    }

    class TestAuthentication : DocumentenApiAuthentication {
        override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
            val filteredRequest = ClientRequest.from(request).headers { headers ->
                headers.setBearerAuth("test")
            }.build()
            return next.exchange(filteredRequest)
        }
    }
}
