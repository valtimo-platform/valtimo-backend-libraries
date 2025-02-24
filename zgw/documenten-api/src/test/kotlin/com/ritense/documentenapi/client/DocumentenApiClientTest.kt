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
import com.ritense.authorization.AuthorizationService
import com.ritense.documentenapi.DocumentenApiAuthentication
import com.ritense.documentenapi.event.DocumentDeleted
import com.ritense.documentenapi.event.DocumentInformatieObjectDownloaded
import com.ritense.documentenapi.event.DocumentInformatieObjectViewed
import com.ritense.documentenapi.event.DocumentListed
import com.ritense.documentenapi.event.DocumentStored
import com.ritense.documentenapi.event.DocumentUpdated
import com.ritense.documentenapi.web.rest.dto.DocumentSearchRequest
import com.ritense.outbox.OutboxService
import com.ritense.outbox.domain.BaseEvent
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.zgw.Rsin
import com.ritense.zgw.domain.Vertrouwelijkheid
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import java.io.InputStream
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import java.util.function.Supplier
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class
DocumentenApiClientTest {

    lateinit var mockDocumentenApi: MockWebServer

    lateinit var objectMapper: ObjectMapper

    lateinit var outboxService: OutboxService

    lateinit var authorizationService: AuthorizationService

    @BeforeAll
    fun setUp() {
        mockDocumentenApi = MockWebServer()
        mockDocumentenApi.start()
        objectMapper = MapperSingleton.get()
        outboxService = mock()
        authorizationService = mock()
        whenever(authorizationService.hasPermission<Any>(any())).thenReturn(true)
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
        val restClientBuilder = RestClient.builder()
        val client = DocumentenApiClient(restClientBuilder, outboxService, objectMapper, mock(), authorizationService)

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
              "locked": true,
              "bestandsdelen": []
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
    fun `should make put call for bestanddelen`() {
        val restClientBuilder = RestClient.builder()
        val client = DocumentenApiClient(restClientBuilder, outboxService, objectMapper, mock(), authorizationService)

        val request = BestandsdelenRequest(
            inhoud = InputStream.nullInputStream(),
            lock = UUID.randomUUID().toString()
        )

        val putResponseBody = """
            {
                "url": "https://example.com/54ff8243-83f9-4fa3-a32e-29970db52ced",
                "volgnummer": 1,
                "omvang": 1234,
                "voltooid": true,
                "lock": "de9c883a-cdfc-493b-9c38-5824e334a1b1"
            }
        """.trimIndent()

        mockDocumentenApi.enqueue(mockResponse(putResponseBody))

        val bestandsdelen = listOf(
            Bestandsdeel(
                "https://www.example.com",
                1,
                0,
                false,
                "de9c883a-cdfc-493b-9c38-5824e334a1b1"
            )
        )

        val createResult = CreateDocumentResult(
            "url",
            "auteur",
            "bestandsnaam.jpg",
            0L,
            LocalDateTime.now(),
            bestandsdelen
        )

        client.storeDocumentInParts(
            TestAuthentication(),
            mockDocumentenApi.url("/").toUri(),
            request,
            createResult,
            "bestand.jpg"
        )

        val recordedRequest = mockDocumentenApi.takeRequest()
        assertNotNull(recordedRequest)

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("PUT", recordedRequest.method)
    }

    @Test
    fun `should send outbox message on saving document`() {
        val restClientBuilder = RestClient.builder()
        val client = DocumentenApiClient(restClientBuilder, outboxService, objectMapper, mock(), authorizationService)
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
              "locked": true,
              "bestandsdelen": []
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

        assertThat(firstEventValue).isInstanceOf(DocumentStored::class.java)
        assertThat(firstEventValue.resultId.toString()).isEqualTo(documentURL)
        assertThat(mappedFirstEventResult.auteur).isEqualTo(result.auteur)
    }

    @Test
    fun `should not send outbox message on error when saving document`() {
        val restClientBuilder = RestClient.builder()
        val client = DocumentenApiClient(restClientBuilder, outboxService, objectMapper, mock(), authorizationService)

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

        assertThrows<HttpClientErrorException> {
            client.storeDocument(
                TestAuthentication(),
                mockDocumentenApi.url("/").toUri(),
                request
            )
        }

        mockDocumentenApi.takeRequest()

        verify(outboxService, times(0)).send(eventCapture.capture())
    }

    @Test
    fun `should send get document request and parse response`() {
        val restClientBuilder = RestClient.builder()
        val client = DocumentenApiClient(restClientBuilder, outboxService, objectMapper, mock(), authorizationService)

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
              "locked": true,
              "bestandsdelen": []
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
        val restClientBuilder = RestClient.builder()
        val client = DocumentenApiClient(restClientBuilder, outboxService, objectMapper, mock(), authorizationService)
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
              "locked": true,
              "bestandsdelen": []
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

        assertThat(firstEventValue).isInstanceOf(DocumentInformatieObjectViewed::class.java)
        assertThat(firstEventValue.resultId).isEqualTo(documentInformatieObjectUrl)
        assertThat(mappedFirstEventResult.auteur).isEqualTo(result.auteur)
    }

    @Test
    fun `should not send outbox message on error retrieving document informatieobject`() {
        val restClientBuilder = RestClient.builder()
        val client = DocumentenApiClient(restClientBuilder, outboxService, objectMapper, mock(), authorizationService)

        mockDocumentenApi.enqueue(mockResponse("").setResponseCode(400))

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        assertThrows<HttpClientErrorException> {
            client.getInformatieObject(
                TestAuthentication(),
                mockDocumentenApi.url("/zaakobjects").toUri(),
            )
        }

        mockDocumentenApi.takeRequest()

        verify(outboxService, times(0)).send(eventCapture.capture())
    }

    @Test
    fun `should send outbox message on download document informatieobject content`() {
        val restClientBuilder = RestClient.builder()
        val client = DocumentenApiClient(restClientBuilder, outboxService, objectMapper, mock(), authorizationService)
        val documentInformatieObjectId = "123"
        val buffer = Buffer()

        //buffer.writeUtf8("test")
        buffer.write(byteArrayOf(72, 73, 32, 84, 79, 77))

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

        assertThat(firstEventValue).isInstanceOf(DocumentInformatieObjectDownloaded::class.java)
        assertThat(firstEventValue.resultId).contains(documentInformatieObjectId)
    }

    @Test
    fun `should not send outbox message on error download document informatieobject content`() {
        val restClientBuilder = RestClient.builder()
        val client = DocumentenApiClient(restClientBuilder, outboxService, objectMapper, mock(), authorizationService)
        val documentInformatieObjectId = "123"

        mockDocumentenApi.enqueue(mockResponse("").setResponseCode(400))

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        assertThrows<HttpClientErrorException> {
            client.downloadInformatieObjectContent(
                TestAuthentication(),
                mockDocumentenApi.url("/").toUri(),
                documentInformatieObjectId
            )
        }

        mockDocumentenApi.takeRequest()

        verify(outboxService, times(0)).send(eventCapture.capture())
    }

    @Test
    fun `should send delete document request and send event`() {
        val restClientBuilder = RestClient.builder()
        val client = DocumentenApiClient(restClientBuilder, outboxService, objectMapper, mock(), authorizationService)
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
        val restClientBuilder = RestClient.builder()
        val client = DocumentenApiClient(restClientBuilder, outboxService, objectMapper, mock(), authorizationService)

        mockDocumentenApi.enqueue(mockResponse("{}").setResponseCode(400))

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        assertThrows<HttpClientErrorException> {
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
        val restClientBuilder = RestClient.builder()
        val client = DocumentenApiClient(restClientBuilder, outboxService, objectMapper, mock(), authorizationService)
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

        mockDocumentenApi.enqueue(mockResponse(responseBody)) // GET
        mockDocumentenApi.enqueue(mockResponse(responseBody)) // PATCH

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
                indicatieGebruiksrecht = true,
                vertrouwelijkheidaanduiding = "openbaar",
                informatieobjecttype = "http://example.com"
            )
        )

        mockDocumentenApi.takeRequest() // Ignore the GET
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

        verify(outboxService, times(2)).send(eventCapture.capture())
        val events = eventCapture.allValues.map { it.get() }
        val viewedEvent = events.single { it is DocumentInformatieObjectViewed }
        assertTrue(viewedEvent.resultId.toString().endsWith("informatie-object/123"))
        assertEquals("com.ritense.documentenapi.client.DocumentInformatieObject", viewedEvent.resultType)
        assertEquals("com.ritense.gzac.drc.enkelvoudiginformatieobject.viewed", viewedEvent.type)

        val updatedEvent = events.single { it is DocumentUpdated }
        assertTrue(updatedEvent.resultId.toString().endsWith("informatie-object/123"))
        assertEquals("com.ritense.documentenapi.client.DocumentInformatieObject", updatedEvent.resultType)
        assertEquals("com.ritense.gzac.drc.document.updated", updatedEvent.type)
        val eventResult: DocumentInformatieObject = objectMapper.readValue(updatedEvent.result.toString())
        assertEquals("auteur", eventResult.auteur)
    }

    @Test
    fun `should not send patch document when document status is definitief`() {
        val restClientBuilder = RestClient.builder()
        val client = DocumentenApiClient(restClientBuilder, outboxService, objectMapper, mock(), authorizationService)
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
              "status": "definitief",
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

        assertThrows<IllegalStateException> {
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
        }

        val recordedRequest = mockDocumentenApi.takeRequest()

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("/informatie-object/123", recordedRequest.path)
        assertEquals("GET", recordedRequest.method)

        //verify request sent
        verify(outboxService, times(1)).send(eventCapture.capture())
        val events = eventCapture.allValues.map { it.get() }
        val viewedEvent = events.single { it is DocumentInformatieObjectViewed }
        assertTrue(viewedEvent.resultId.toString().endsWith("informatie-object/123"))
        assertEquals("com.ritense.documentenapi.client.DocumentInformatieObject", viewedEvent.resultType)
        assertEquals("com.ritense.gzac.drc.enkelvoudiginformatieobject.viewed", viewedEvent.type)

        assertTrue(events.none { it is DocumentUpdated })
    }

    @Test
    fun `should not send outbox message on error updating document informatieobject`() {
        val restClientBuilder = RestClient.builder()
        val client = DocumentenApiClient(restClientBuilder, outboxService, objectMapper, mock(), authorizationService)

        mockDocumentenApi.enqueue(mockResponse("{}").setResponseCode(400))

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        assertThrows<HttpClientErrorException> {
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

    @Test
    fun `search result should return page of values`() {
        val pageable = Pageable.ofSize(10)
        val documentSearchRequest = DocumentSearchRequest(
            zaakUrl = URI("http://example.com/zaak/123"),
        )
        val documentSearchResult = doDocumentSearchRequest(pageable, documentSearchRequest)

        assertEquals("Bearer test", documentSearchResult.recordedRequest.getHeader("Authorization"))
        assertEquals("/enkelvoudiginformatieobjecten", documentSearchResult.recordedRequest.path?.substringBefore("?"))
        assertEquals("GET", documentSearchResult.recordedRequest.method)
        assertEquals(5, documentSearchResult.page.content.size)

        val queryParameters = parseQueryString(documentSearchResult.recordedRequest.requestUrl.toString())
        assertEquals("http://example.com/zaak/123", queryParameters["objectinformatieobjecten__object"])

        //verify result
        val result = documentSearchResult.page.content[0]

        assertEquals(URI("http://api.example.org/informatieobjecten/600a5a20-97d6-46f2-8424-15315e53e9c2"), result.url)
        assertEquals("identificatie", result.identificatie)
        assertEquals(Rsin("621248691"), result.bronorganisatie)
        assertEquals(LocalDate.of(2019, 8, 24), result.creatiedatum)
        assertEquals("titel", result.titel)
        assertEquals(Vertrouwelijkheid.OPENBAAR, result.vertrouwelijkheidaanduiding)
        assertEquals("auteur", result.auteur)
        assertEquals(DocumentStatusType.DEFINITIEF, result.status)
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

        //verify event
        val listedEvent = documentSearchResult.event as DocumentListed
        assertTrue(listedEvent.resultId == null)
        assertEquals("List<com.ritense.documentenapi.client.DocumentInformatieObject>", listedEvent.resultType)
        assertEquals("com.ritense.gzac.drc.document.listed", listedEvent.type)
        assertEquals(5, listedEvent.result?.size())
    }

    @Test
    fun `search should return single page`() {
        val pageable = Pageable.ofSize(2)
        val documentSearchRequest = DocumentSearchRequest(
            zaakUrl = URI("http://example.com/zaak/123"),
        )
        val documentSearchResult = doDocumentSearchRequest(pageable, documentSearchRequest)

        val queryParameters = parseQueryString(documentSearchResult.recordedRequest.requestUrl.toString())
        assertEquals("http://example.com/zaak/123", queryParameters["objectinformatieobjecten__object"])
        assertEquals("1", queryParameters["page"])

        //verify result is first 2 items
        assertEquals(2, documentSearchResult.page.content.size)
        assertEquals(
            "http://api.example.org/informatieobjecten/600a5a20-97d6-46f2-8424-15315e53e9c2",
            documentSearchResult.page.content[0].url.toString()
        )
        assertEquals(
            "http://api.example.org/informatieobjecten/10545984-7748-4234-b7f3-7b4aa3b2721a",
            documentSearchResult.page.content[1].url.toString()
        )

        //verify event
        val listedEvent = documentSearchResult.event as DocumentListed
        assertTrue(listedEvent.resultId == null)
        assertEquals("List<com.ritense.documentenapi.client.DocumentInformatieObject>", listedEvent.resultType)
        assertEquals("com.ritense.gzac.drc.document.listed", listedEvent.type)
        assertEquals(2, listedEvent.result?.size())
    }

    @Test
    fun `search should return second page`() {
        val pageable = Pageable.ofSize(2).withPage(1)
        val documentSearchRequest = DocumentSearchRequest(
            zaakUrl = URI("http://example.com/zaak/123"),
        )
        val documentSearchResult = doDocumentSearchRequest(pageable, documentSearchRequest)

        val queryParameters = parseQueryString(documentSearchResult.recordedRequest.requestUrl.toString())
        assertEquals("http://example.com/zaak/123", queryParameters["objectinformatieobjecten__object"])
        assertEquals("1", queryParameters["page"])

        //verify result is next 2 items
        assertEquals(2, documentSearchResult.page.content.size)
        assertEquals(
            "http://api.example.org/informatieobjecten/b8a3c2ea-097b-4b9f-a595-9d9f23cde95e",
            documentSearchResult.page.content[0].url.toString()
        )
        assertEquals(
            "http://api.example.org/informatieobjecten/912dcb8d-2c51-4f7f-80e7-7375ab5fd2a9",
            documentSearchResult.page.content[1].url.toString()
        )

        //verify event
        val listedEvent = documentSearchResult.event as DocumentListed
        assertTrue(listedEvent.resultId == null)
        assertEquals("List<com.ritense.documentenapi.client.DocumentInformatieObject>", listedEvent.resultType)
        assertEquals("com.ritense.gzac.drc.document.listed", listedEvent.type)
        assertEquals(2, listedEvent.result?.size())
    }

    @Test
    fun `search should return first results of second api page`() {

        val pageable = Pageable.ofSize(2).withPage(50)
        val documentSearchRequest = DocumentSearchRequest(
            zaakUrl = URI("http://example.com/zaak/123"),
        )
        val documentSearchResult = doDocumentSearchRequest(pageable, documentSearchRequest)

        val queryParameters = parseQueryString(documentSearchResult.recordedRequest.requestUrl.toString())
        assertEquals("http://example.com/zaak/123", queryParameters["objectinformatieobjecten__object"])
        assertEquals("2", queryParameters["page"])

        //verify result is first 2 items
        assertEquals(2, documentSearchResult.page.content.size)
        assertEquals(
            "http://api.example.org/informatieobjecten/600a5a20-97d6-46f2-8424-15315e53e9c2",
            documentSearchResult.page.content[0].url.toString()
        )
        assertEquals(
            "http://api.example.org/informatieobjecten/10545984-7748-4234-b7f3-7b4aa3b2721a",
            documentSearchResult.page.content[1].url.toString()
        )

        //verify event
        val listedEvent = documentSearchResult.event as DocumentListed
        assertTrue(listedEvent.resultId == null)
        assertEquals("List<com.ritense.documentenapi.client.DocumentInformatieObject>", listedEvent.resultType)
        assertEquals("com.ritense.gzac.drc.document.listed", listedEvent.type)
        assertEquals(2, listedEvent.result?.size())
    }

    @Test
    fun `search with unsupported page size should throw exception`() {

        val pageable = Pageable.ofSize(3) // 100 is not evenly divisible by 3
        val documentSearchRequest = DocumentSearchRequest(
            zaakUrl = URI("http://example.com/zaak/123"),
        )
        val exception = assertThrows<IllegalArgumentException> {
            doDocumentSearchRequest(pageable, documentSearchRequest, true)
        }

        assertEquals("Page size is not supported", exception.message)
    }

    @Test
    fun `search without zaakUrl should throw exception`() {

        val pageable = Pageable.ofSize(10)
        val documentSearchRequest = DocumentSearchRequest()

        val exception = assertThrows<IllegalArgumentException> {
            doDocumentSearchRequest(pageable, documentSearchRequest, true)
        }

        assertEquals("Zaak URL is required", exception.message)
    }

    @Test
    fun `search should send all optional search parameters`() {

        val pageable = Pageable.ofSize(2).withPage(0)
        val documentSearchRequest = DocumentSearchRequest(
            informatieobjecttype = "http://example.com/informatieobjecttype/123",
            titel = "title",
            vertrouwelijkheidaanduiding = "confidential",
            creatiedatumFrom = LocalDate.of(2020, 5, 3),
            creatiedatumTo = LocalDate.of(2020, 5, 4),
            auteur = "author",
            trefwoorden = listOf("tag1", "tag2"),
            zaakUrl = URI("http://example.com/zaak/123"),
        )
        val documentSearchResult = doDocumentSearchRequest(pageable, documentSearchRequest)

        val queryParameters = parseQueryString(documentSearchResult.recordedRequest.requestUrl.toString())
        assertEquals("http://example.com/zaak/123", queryParameters["objectinformatieobjecten__object"])
        assertEquals("1", queryParameters["page"])
        assertEquals("http://example.com/informatieobjecttype/123", queryParameters["informatieobjecttype"])
        assertEquals("title", queryParameters["titel"])
        assertEquals("confidential", queryParameters["vertrouwelijkheidaanduiding"])
        assertEquals("author", queryParameters["auteur"])
        assertEquals("2020-05-03", queryParameters["creatiedatum__gte"])
        assertEquals("2020-05-04", queryParameters["creatiedatum__lte"])
        assertEquals("tag1,tag2", queryParameters["trefwoorden"])
    }

    @Test
    fun `search should sort on known sort option`() {
        val pageable = PageRequest.of(0, 10, Sort.by("titel"))
        val documentSearchRequest = DocumentSearchRequest(
            zaakUrl = URI("http://example.com/zaak/123"),
        )
        val documentSearchResult = doDocumentSearchRequest(pageable, documentSearchRequest)

        val queryParameters = parseQueryString(documentSearchResult.recordedRequest.requestUrl.toString())
        assertEquals("http://example.com/zaak/123", queryParameters["objectinformatieobjecten__object"])
        assertEquals("titel", queryParameters["ordering"])
    }

    @Test
    fun `search should sort on known sort option descending`() {
        val pageable = PageRequest.of(0, 10, Sort.by("titel").descending())
        val documentSearchRequest = DocumentSearchRequest(
            zaakUrl = URI("http://example.com/zaak/123"),
        )
        val documentSearchResult = doDocumentSearchRequest(pageable, documentSearchRequest)

        val queryParameters = parseQueryString(documentSearchResult.recordedRequest.requestUrl.toString())
        assertEquals("http://example.com/zaak/123", queryParameters["objectinformatieobjecten__object"])
        assertEquals("-titel", queryParameters["ordering"])
    }

    @Test
    fun `search should sort on multiple known sort options`() {
        val pageable = PageRequest.of(0, 10, Sort.by("titel").descending().and(Sort.by("auteur").ascending()))
        val documentSearchRequest = DocumentSearchRequest(
            zaakUrl = URI("http://example.com/zaak/123"),
        )
        val documentSearchResult = doDocumentSearchRequest(pageable, documentSearchRequest)

        val queryParameters = parseQueryString(documentSearchResult.recordedRequest.requestUrl.toString())
        assertEquals("http://example.com/zaak/123", queryParameters["objectinformatieobjecten__object"])
        assertEquals("-titel,auteur", queryParameters["ordering"])
    }

    @Test
    fun `search should throw exception when sorting by unknown sort option`() {
        val pageable = PageRequest.of(0, 10, Sort.by("something"))
        val documentSearchRequest = DocumentSearchRequest(
            zaakUrl = URI("http://example.com/zaak/123"),
        )
        assertThrows<IllegalArgumentException> {
            doDocumentSearchRequest(pageable, documentSearchRequest, true)
        }
    }

    private fun parseQueryString(url: String?): Map<String, String> {
        return url?.substringAfter("?")?.split("&")?.associate {
            val (key, value) = it.split("=")
            key to value
        } ?: emptyMap()
    }

    private fun doDocumentSearchRequest(
        pageable: Pageable,
        documentSearchRequest: DocumentSearchRequest,
        expectException: Boolean = false
    ): DocumentSearchResult {
        val restClientBuilder = RestClient.builder()
        val client = DocumentenApiClient(restClientBuilder, outboxService, objectMapper, mock(), authorizationService)
        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        // prevent queuing of response when exception is expected to avoid other tests breaking with old data in the queue
        if (!expectException) {
            mockDocumentenApi.enqueue(mockResponseFromFile("/config/documenten-api/document-page.json"))
        }

        val page = client.getInformatieObjecten(
            TestAuthentication(),
            mockDocumentenApi.url("/").toUri(),
            pageable,
            documentSearchRequest
        )

        val recordedRequest = mockDocumentenApi.takeRequest()
        verify(outboxService).send(eventCapture.capture())

        return DocumentSearchResult(
            page,
            recordedRequest,
            eventCapture.firstValue.get()
        )
    }

    class DocumentSearchResult(
        val page: Page<DocumentInformatieObject>,
        val recordedRequest: RecordedRequest,
        val event: BaseEvent
    )

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

    private fun mockResponseFromFile(fileName: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(200)
            .setBody(this::class.java.getResource(fileName).readText(Charsets.UTF_8))
    }

    class TestAuthentication : DocumentenApiAuthentication {
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
