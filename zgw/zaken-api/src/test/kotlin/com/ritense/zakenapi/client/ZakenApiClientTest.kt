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

package com.ritense.zakenapi.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.outbox.OutboxService
import com.ritense.outbox.domain.BaseEvent
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.zakenapi.ZakenApiAuthentication
import com.ritense.zakenapi.domain.CreateZaakRequest
import com.ritense.zakenapi.domain.CreateZaakResponse
import com.ritense.zakenapi.domain.CreateZaakResultaatRequest
import com.ritense.zakenapi.domain.CreateZaakResultaatResponse
import com.ritense.zakenapi.domain.CreateZaakStatusRequest
import com.ritense.zakenapi.domain.CreateZaakStatusResponse
import com.ritense.zakenapi.domain.Opschorting
import com.ritense.zakenapi.domain.Verlenging
import com.ritense.zakenapi.domain.ZaakInformatieObject
import com.ritense.zakenapi.domain.ZaakObject
import com.ritense.zakenapi.domain.ZaakResponse
import com.ritense.zakenapi.domain.ZaakopschortingRequest
import com.ritense.zakenapi.domain.ZaakopschortingResponse
import com.ritense.zakenapi.domain.rol.BetrokkeneType
import com.ritense.zakenapi.domain.rol.IndicatieMachtiging
import com.ritense.zakenapi.domain.rol.Rol
import com.ritense.zakenapi.domain.rol.RolNatuurlijkPersoon
import com.ritense.zakenapi.domain.rol.RolNietNatuurlijkPersoon
import com.ritense.zakenapi.domain.rol.RolType
import com.ritense.zakenapi.domain.rol.ZaakRolOmschrijving
import com.ritense.zakenapi.event.DocumentLinkedToZaak
import com.ritense.zakenapi.event.ZaakCreated
import com.ritense.zakenapi.event.ZaakInformatieObjectenListed
import com.ritense.zakenapi.event.ZaakObjectenListed
import com.ritense.zakenapi.event.ZaakOpschortingUpdated
import com.ritense.zakenapi.event.ZaakResultaatCreated
import com.ritense.zakenapi.event.ZaakRolCreated
import com.ritense.zakenapi.event.ZaakRollenListed
import com.ritense.zakenapi.event.ZaakStatusCreated
import com.ritense.zakenapi.event.ZaakViewed
import com.ritense.zgw.Rsin
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID
import java.util.function.Supplier
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ZakenApiClientTest {

    lateinit var mockApi: MockWebServer

    lateinit var objectMapper: ObjectMapper

    lateinit var outboxService: OutboxService

    @BeforeAll
    fun setUp() {
        mockApi = MockWebServer()
        mockApi.start()
        objectMapper = MapperSingleton.get()
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
    fun `should send link document request and parse response`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val responseBody = """
            {
              "url": "https://example.com",
              "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
              "informatieobject": "https://example.com",
              "zaak": "https://example.com",
              "aardRelatieWeergave": "Hoort bij, omgekeerd: kent",
              "titel": "string",
              "beschrijving": "string",
              "registratiedatum": "2019-08-24T14:15:22Z"
            }
        """.trimIndent()

        mockApi.enqueue(mockResponse(responseBody))

        val result = client.linkDocument(
            TestAuthentication(),
            URI(mockApi.url("/").toString()),
            LinkDocumentRequest(
                "https://example.com",
                "https://example.com",
                "title",
                "description"
            )
        )

        val recordedRequest = mockApi.takeRequest()
        val requestString = recordedRequest.body.readUtf8()
        val parsedOutput: Map<String, Any> = objectMapper.readValue(requestString)

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))

        assertEquals("https://example.com", parsedOutput["informatieobject"])
        assertEquals("https://example.com", parsedOutput["zaak"])
        assertEquals("title", parsedOutput["titel"])
        assertEquals("description", parsedOutput["beschrijving"])

        assertEquals("https://example.com", result.url)
        assertEquals("https://example.com", result.informatieobject)
        assertEquals("https://example.com", result.zaak)
        assertEquals(UUID.fromString("095be615-a8ad-4c33-8e9c-c7612fbf6c9f"), result.uuid)
        assertEquals("string", result.titel)
        assertEquals("string", result.beschrijving)
        assertEquals("Hoort bij, omgekeerd: kent", result.aardRelatieWeergave)
        assertEquals(LocalDateTime.of(2019, 8, 24, 14, 15, 22), result.registratiedatum)
    }

    @Test
    fun `should not include null fields when creating zaakstatus`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        mockApi.enqueue(mockResponse("").setResponseCode(400))

        assertThrows<HttpClientErrorException> {
            client.createZaakStatus(
                TestAuthentication(),
                URI(mockApi.url("/").toString()),
                CreateZaakStatusRequest(
                    zaak = URI("https://example.com"),
                    datumStatusGezet = LocalDateTime.parse("2023-03-03T03:03:00"),
                    statustype = URI("https://example.com"),
                    statustoelichting = null
                )
            )
        }

        val recordedRequest = mockApi.takeRequest()
        val requestString = recordedRequest.body.readUtf8()
        val parsedOutput: Map<String, Any> = objectMapper.readValue(requestString)

        assertEquals("https://example.com", parsedOutput["zaak"])
        assertEquals("https://example.com", parsedOutput["statustype"])
        assertNull(parsedOutput["statustoelichting"])
    }

    @Test
    fun `should send outbox message on linking document`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val uuid = "095be615-a8ad-4c33-8e9c-c7612fbf6c9f"
        val responseBody = """
            {
              "url": "https://example.com",
              "uuid": "$uuid",
              "informatieobject": "https://example.com",
              "zaak": "https://example.com",
              "aardRelatieWeergave": "Hoort bij, omgekeerd: kent",
              "titel": "string",
              "beschrijving": "string",
              "registratiedatum": "2019-08-24T14:15:22Z"
            }
        """.trimIndent()

        mockApi.enqueue(mockResponse(responseBody))

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        client.linkDocument(
            TestAuthentication(),
            URI(mockApi.url("/").toString()),
            LinkDocumentRequest(
                "https://example.com",
                "https://example.com",
                "title",
                "description"
            )
        )

        mockApi.takeRequest()

        verify(outboxService).send(eventCapture.capture())

        val firstEventValue = eventCapture.firstValue.get()
        val mappedResult: LinkDocumentResult = objectMapper.readValue(firstEventValue.result.toString())
        val mappedResponseBody: LinkDocumentResult = objectMapper.readValue(responseBody)

        assertThat(firstEventValue).isInstanceOf(DocumentLinkedToZaak::class.java)
        Assertions.assertThat(firstEventValue.resultId).isEqualTo(uuid)
        Assertions.assertThat(mappedResult.beschrijving).isEqualTo(mappedResponseBody.beschrijving)
    }

    @Test
    fun `should not send outbox message on failing to link document`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        mockApi.enqueue(mockResponse("").setResponseCode(400))

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        assertThrows<HttpClientErrorException> {
            client.linkDocument(
                TestAuthentication(),
                URI(mockApi.url("/").toString()),
                LinkDocumentRequest(
                    "https://example.com",
                    "https://example.com",
                    "title",
                    "description"
                )
            )
        }

        mockApi.takeRequest()

        verify(outboxService, times(0)).send(eventCapture.capture())
    }

    @Test
    fun `should send get zaakobjecten request and parse response`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val responseBody = """
            {
              "count": 1,
              "next": "https://example.com",
              "previous": "https://example.com",
              "results": [
                {
                  "url": "https://example.com",
                  "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
                  "zaak": "https://example.com",
                  "object": "https://example.com",
                  "objectType": "adres",
                  "objectTypeOverige": "string",
                  "relatieomschrijving": "string"
                }
              ]
            }
        """.trimIndent()

        mockApi.enqueue(mockResponse(responseBody))

        val result = client.getZaakObjecten(
            TestAuthentication(),
            URI(mockApi.url("/").toString()),
            URI("https://example.com"),
            1
        )

        val recordedRequest = mockApi.takeRequest()
        val requestUrl = recordedRequest.requestUrl

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("https://example.com", requestUrl?.queryParameter("zaak"))
        assertEquals("1", requestUrl?.queryParameter("page"))

        assertEquals(1, result.count)
        assertEquals(URI("https://example.com"), result.next)
        assertEquals(URI("https://example.com"), result.previous)
        assertEquals(URI("https://example.com"), result.results[0].url)
        assertEquals(UUID.fromString("095be615-a8ad-4c33-8e9c-c7612fbf6c9f"), result.results[0].uuid)
        assertEquals(URI("https://example.com"), result.results[0].zaakUrl)
        assertEquals(URI("https://example.com"), result.results[0].objectUrl)
        assertEquals("adres", result.results[0].objectType)
        assertEquals("string", result.results[0].objectTypeOverige)
        assertEquals("string", result.results[0].relatieomschrijving)
    }

    @Test
    fun `should send outbox message on retrieving zaakobjecten`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val responseBody = """
            {
              "count": 1,
              "next": "https://example.com",
              "previous": "https://example.com",
              "results": [
                {
                  "url": "https://example.com",
                  "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
                  "zaak": "https://example.com",
                  "object": "https://example.com",
                  "objectType": "adres",
                  "objectTypeOverige": "string",
                  "relatieomschrijving": "string"
                }
              ]
            }
        """.trimIndent()

        mockApi.enqueue(mockResponse(responseBody))

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        val result = client.getZaakObjecten(
            TestAuthentication(),
            URI(mockApi.url("/").toString()),
            URI("https://example.com"),
            1
        )

        mockApi.takeRequest()

        verify(outboxService).send(eventCapture.capture())

        val firstEventValue = eventCapture.firstValue.get()
        val mappedFirstEventResult: List<ZaakObject> = objectMapper.readValue(firstEventValue.result.toString())

        assertThat(firstEventValue).isInstanceOf(ZaakObjectenListed::class.java)
        Assertions.assertThat(result.results.first().relatieomschrijving)
            .isEqualTo(mappedFirstEventResult.first().relatieomschrijving)
    }

    @Test
    fun `should not send outbox message on failing to retrieve zaakobjecten`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        mockApi.enqueue(mockResponse("").setResponseCode(400))

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        assertThrows<HttpClientErrorException> {
            client.getZaakObjecten(
                TestAuthentication(),
                URI(mockApi.url("/").toString()),
                URI("https://example.com"),
                1
            )
        }

        mockApi.takeRequest()

        verify(outboxService, times(0)).send(eventCapture.capture())
    }

    @Test
    fun `should send get zaakinformatieobjecten request and parse response`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val informatieObjectJson = """
            {
                "url": "http://example.com",
                "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
                "informatieobject": "http://example.com",
                "zaak": "http://example.com",
                "aardRelatieWeergave": "Hoort bij, omgekeerd: kent",
                "titel": "test",
                "beschrijving": "test omschrijving",
                "registratiedatum": "2019-08-24T14:15:22Z",
                "vernietigingsdatum": "2019-08-24T14:15:22Z",
                "status": "http://example.com"
            }
        """.trimIndent()
        val responseBody = """
            [
                $informatieObjectJson, $informatieObjectJson
            ]
        """.trimIndent()

        mockApi.enqueue(mockResponse(responseBody))

        val result = client.getZaakInformatieObjecten(
            TestAuthentication(),
            URI(mockApi.url("/").toString()),
            URI("https://example.com")
        )

        val recordedRequest = mockApi.takeRequest()
        val requestUrl = recordedRequest.requestUrl

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("https://example.com", requestUrl?.queryParameter("zaak"))
        assertEquals(2, result.size)

        val value = result.first()
        assertEquals(URI("http://example.com"), value.url)
        assertEquals(UUID.fromString("095be615-a8ad-4c33-8e9c-c7612fbf6c9f"), value.uuid)
        assertEquals(URI("http://example.com"), value.informatieobject)
        assertEquals(URI("http://example.com"), value.zaak)
        assertEquals("Hoort bij, omgekeerd: kent", value.aardRelatieWeergave)
        assertEquals("test", value.titel)
        assertEquals("test omschrijving", value.beschrijving)
        assertEquals(ZonedDateTime.parse("2019-08-24T14:15:22Z").toLocalDateTime(), value.registratiedatum)
        assertEquals(ZonedDateTime.parse("2019-08-24T14:15:22Z").toLocalDateTime(), value.vernietigingsdatum)
        assertEquals(URI("http://example.com"), value.status)
    }

    @Test
    fun `should send outbox message on retrieving zaakinformatieobjecten`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val informatieObjectJson = """
            {
                "url": "http://example.com",
                "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
                "informatieobject": "http://example.com",
                "zaak": "http://example.com",
                "aardRelatieWeergave": "Hoort bij, omgekeerd: kent",
                "titel": "test",
                "beschrijving": "test omschrijving",
                "registratiedatum": "2019-08-24T14:15:22Z",
                "vernietigingsdatum": "2019-08-24T14:15:22Z",
                "status": "http://example.com"
            }
        """.trimIndent()
        val responseBody = """
            [
                $informatieObjectJson, $informatieObjectJson
            ]
        """.trimIndent()

        mockApi.enqueue(mockResponse(responseBody))

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        val result = client.getZaakInformatieObjecten(
            TestAuthentication(),
            URI(mockApi.url("/").toString()),
            URI("https://example.com")
        )

        mockApi.takeRequest()

        verify(outboxService).send(eventCapture.capture())

        val firstEventValue = eventCapture.firstValue.get()
        val mappedFirstEventResult: List<ZaakInformatieObject> =
            objectMapper.readValue(firstEventValue.result.toString())

        assertThat(firstEventValue).isInstanceOf(ZaakInformatieObjectenListed::class.java)
        Assertions.assertThat(result.first().beschrijving).isEqualTo(mappedFirstEventResult.first().beschrijving)
    }

    @Test
    fun `should not send outbox message on failing to retrieve zaakinformatieobjecten`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        mockApi.enqueue(mockResponse("").setResponseCode(400))

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        assertThrows<HttpClientErrorException> {
            client.getZaakInformatieObjecten(
                TestAuthentication(),
                URI(mockApi.url("/").toString()),
                URI("https://example.com")
            )
        }

        mockApi.takeRequest()

        verify(outboxService, times(0)).send(eventCapture.capture())
    }

    @Test
    fun `should send get zaakrollen request and parse response`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val responseBody = """
            {
              "count": 1,
              "next": "https://example.com/next",
              "previous": "https://example.com/previous",
              "results": [
                {
                  "zaak": "https://example.com/zaak",
                  "betrokkene": "https://example.com/betrokkene",
                  "betrokkeneType": "natuurlijk_persoon",
                  "roltype": "https://example.com/roltype",
                  "roltoelichting": "initiator",
                  "betrokkeneIdentificatie": {
                    "inpBsn": "059861095"
                  },
                  "unknownProperty": "value"
                }
              ]
            }
        """.trimIndent()

        mockApi.enqueue(mockResponse(responseBody))

        val result = client.getZaakRollen(
            TestAuthentication(),
            URI(mockApi.url("/").toString()),
            URI("https://example.com"),
            1,
            RolType.INITIATOR
        )

        val recordedRequest = mockApi.takeRequest()
        val requestUrl = recordedRequest.requestUrl

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))
        assertEquals("https://example.com", requestUrl?.queryParameter("zaak"))
        assertEquals("1", requestUrl?.queryParameter("page"))

        assertEquals(1, result.count)
        assertEquals(URI("https://example.com/next"), result.next)
        assertEquals(URI("https://example.com/previous"), result.previous)
        assertEquals(URI("https://example.com/betrokkene"), result.results.first().betrokkene)
        assertEquals(BetrokkeneType.NATUURLIJK_PERSOON, result.results.first().betrokkeneType)
        assertEquals(URI("https://example.com/zaak"), result.results.first().zaak)
        assertEquals(URI("https://example.com/roltype"), result.results.first().roltype)
        assertEquals("initiator", result.results.first().roltoelichting)
        assertEquals(RolNatuurlijkPersoon(inpBsn = "059861095"), result.results.first().betrokkeneIdentificatie)
    }

    @Test
    fun `should send outbox message on retrieving zaakrollen`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val responseBody = """
            {
              "count": 1,
              "next": "https://example.com/next",
              "previous": "https://example.com/previous",
              "results": [
                {
                  "zaak": "https://example.com/zaak",
                  "betrokkene": "https://example.com/betrokkene",
                  "betrokkeneType": "natuurlijk_persoon",
                  "roltype": "https://example.com/roltype",
                  "roltoelichting": "initiator",
                  "betrokkeneIdentificatie": {
                    "inpBsn": "059861095"
                  },
                  "unknownProperty": "value"
                }
              ]
            }
        """.trimIndent()

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        mockApi.enqueue(mockResponse(responseBody))

        val result = client.getZaakRollen(
            TestAuthentication(),
            URI(mockApi.url("/").toString()),
            URI("https://example.com"),
            1,
            RolType.INITIATOR
        )

        mockApi.takeRequest()

        verify(outboxService).send(eventCapture.capture())

        val firstEventValue = eventCapture.firstValue.get()
        val mappedFirstEventResult: List<Rol> = objectMapper.readValue(firstEventValue.result.toString())

        assertThat(firstEventValue).isInstanceOf(ZaakRollenListed::class.java)
        Assertions.assertThat(result.results.first().roltoelichting)
            .isEqualTo(mappedFirstEventResult.first().roltoelichting)
    }

    @Test
    fun `should not send outbox message on failing to retrieve zaakrollen`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        mockApi.enqueue(mockResponse("").setResponseCode(400))

        assertThrows<HttpClientErrorException> {
            client.getZaakRollen(
                TestAuthentication(),
                URI(mockApi.url("/").toString()),
                URI("https://example.com"),
                1,
                RolType.INITIATOR
            )
        }

        mockApi.takeRequest()

        verify(outboxService, times(0)).send(eventCapture.capture())
    }

    @Test
    fun `should send create natuurlijk persoon zaakrol request and parse response`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val responseBody = """
            {
              "url": "https://example.com/rol/d31cd83f-11da-4932-bde8-a9123c9821d3",
              "uuid": "d31cd83f-11da-4932-bde8-a9123c9821d3",
              "zaak": "https://example.com/zaak",
              "betrokkene": "https://example.com/betrokkene",
              "betrokkeneType": "natuurlijk_persoon",
              "roltype": "https://example.com/roltype",
              "omschrijving": "omschrijving",
              "omschrijvingGeneriek": "initiator",
              "roltoelichting": "roltoelichting",
              "registratiedatum": "2019-08-24T14:15:22Z",
              "indicatieMachtiging": "gemachtigde",
              "betrokkeneIdentificatie": {
                "inpBsn": "inpBsn",
                "anpIdentificatie": "anpIdentificatie",
                "inpA_nummer": "inpA_nummer",
                "geslachtsnaam": "geslachtsnaam",
                "voorvoegselGeslachtsnaam": "voorvoegselGeslachtsnaam",
                "voorletters": "voorletters",
                "voornamen": "voornamen",
                "geslachtsaanduiding": "m",
                "geboortedatum": "geboortedatum",
                "verblijfsadres": {
                  "aoaIdentificatie": "string",
                  "wplWoonplaatsNaam": "string",
                  "gorOpenbareRuimteNaam": "string",
                  "aoaPostcode": "string",
                  "aoaHuisnummer": 0,
                  "aoaHuisletter": "s",
                  "aoaHuisnummertoevoeging": "a",
                  "inpLocatiebeschrijving": "string"
                },
                "subVerblijfBuitenland": {
                  "lndLandcode": "stri",
                  "lndLandnaam": "string",
                  "subAdresBuitenland_1": "string",
                  "subAdresBuitenland_2": "string",
                  "subAdresBuitenland_3": "string"
                }
              }
            }
        """.trimIndent()

        mockApi.enqueue(mockResponse(responseBody))

        val result = client.createZaakRol(
            TestAuthentication(),
            URI(mockApi.url("/").toString()),
            Rol(
                zaak = URI("https://example.com/zaak"),
                betrokkeneType = BetrokkeneType.NATUURLIJK_PERSOON,
                roltype = URI("https://example.com/roltype"),
                roltoelichting = "test",
                betrokkeneIdentificatie = RolNatuurlijkPersoon()
            )
        )

        val recordedRequest = mockApi.takeRequest()
        // val requestUrl = recordedRequest.requestUrl

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))

        assertEquals(URI("https://example.com/rol/d31cd83f-11da-4932-bde8-a9123c9821d3"), result.url)
        assertEquals(UUID.fromString("d31cd83f-11da-4932-bde8-a9123c9821d3"), result.uuid)
        assertEquals(URI("https://example.com/zaak"), result.zaak)
        assertEquals(URI("https://example.com/betrokkene"), result.betrokkene)
        assertEquals(BetrokkeneType.NATUURLIJK_PERSOON, result.betrokkeneType)
        assertEquals(URI("https://example.com/roltype"), result.roltype)
        assertEquals("omschrijving", result.omschrijving)
        assertEquals(ZaakRolOmschrijving.INITIATOR, result.omschrijvingGeneriek)
        assertEquals("roltoelichting", result.roltoelichting)
        assertEquals(LocalDateTime.of(2019, 8, 24, 14, 15, 22), result.registratiedatum)
        assertEquals(IndicatieMachtiging.GEMACHTIGDE, result.indicatieMachtiging)

        val betrokkeneIdentificatie = result.betrokkeneIdentificatie as RolNatuurlijkPersoon
        assertEquals("inpBsn", betrokkeneIdentificatie.inpBsn)
        assertEquals("anpIdentificatie", betrokkeneIdentificatie.anpIdentificatie)
        assertEquals("inpA_nummer", betrokkeneIdentificatie.inpA_nummer)
        assertEquals("geslachtsnaam", betrokkeneIdentificatie.geslachtsnaam)
        assertEquals("voorvoegselGeslachtsnaam", betrokkeneIdentificatie.voorvoegselGeslachtsnaam)
        assertEquals("voorletters", betrokkeneIdentificatie.voorletters)
        assertEquals("voornamen", betrokkeneIdentificatie.voornamen)
        assertEquals("m", betrokkeneIdentificatie.geslachtsaanduiding)
        assertEquals("geboortedatum", betrokkeneIdentificatie.geboortedatum)
    }

    @Test
    fun `should send create niet-natuurlijk persoon zaakrol request and parse response`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val responseBody = """
            {
              "url": "https://example.com/rol/d31cd83f-11da-4932-bde8-a9123c9821d3",
              "uuid": "d31cd83f-11da-4932-bde8-a9123c9821d3",
              "zaak": "https://example.com/zaak",
              "betrokkene": "https://example.com/betrokkene",
              "betrokkeneType": "niet_natuurlijk_persoon",
              "roltype": "https://example.com/roltype",
              "omschrijving": "omschrijving",
              "omschrijvingGeneriek": "initiator",
              "roltoelichting": "roltoelichting",
              "registratiedatum": "2019-08-24T14:15:22Z",
              "indicatieMachtiging": "gemachtigde",
              "betrokkeneIdentificatie": {
                "annIdentificatie": "annIdentificatie",
                "innNnpId": "innNnpId",
                "statutaireNaam": "statutaireNaam",
                "innRechtsvorm": "besloten_vennootschap",
                "bezoekadres": "bezoekadres"
              }
            }
        """.trimIndent()

        mockApi.enqueue(mockResponse(responseBody))

        val result = client.createZaakRol(
            TestAuthentication(),
            URI(mockApi.url("/").toString()),
            Rol(
                zaak = URI("https://example.com/zaak"),
                betrokkeneType = BetrokkeneType.NIET_NATUURLIJK_PERSOON,
                roltype = URI("https://example.com/roltype"),
                roltoelichting = "test",
                betrokkeneIdentificatie = RolNietNatuurlijkPersoon(
                    annIdentificatie = "annIdentificatie"
                )
            )
        )

        val recordedRequest = mockApi.takeRequest()
        // val requestUrl = recordedRequest.requestUrl

        assertEquals("Bearer test", recordedRequest.getHeader("Authorization"))

        assertEquals(URI("https://example.com/rol/d31cd83f-11da-4932-bde8-a9123c9821d3"), result.url)
        assertEquals(UUID.fromString("d31cd83f-11da-4932-bde8-a9123c9821d3"), result.uuid)
        assertEquals(URI("https://example.com/zaak"), result.zaak)
        assertEquals(URI("https://example.com/betrokkene"), result.betrokkene)
        assertEquals(BetrokkeneType.NIET_NATUURLIJK_PERSOON, result.betrokkeneType)
        assertEquals(URI("https://example.com/roltype"), result.roltype)
        assertEquals("omschrijving", result.omschrijving)
        assertEquals(ZaakRolOmschrijving.INITIATOR, result.omschrijvingGeneriek)
        assertEquals("roltoelichting", result.roltoelichting)
        assertEquals(LocalDateTime.of(2019, 8, 24, 14, 15, 22), result.registratiedatum)
        assertEquals(IndicatieMachtiging.GEMACHTIGDE, result.indicatieMachtiging)

        val betrokkeneIdentificatie = result.betrokkeneIdentificatie as RolNietNatuurlijkPersoon
        assertEquals("annIdentificatie", betrokkeneIdentificatie.annIdentificatie)
        assertEquals("innNnpId", betrokkeneIdentificatie.innNnpId)
        assertEquals("statutaireNaam", betrokkeneIdentificatie.statutaireNaam)
        assertEquals("besloten_vennootschap", betrokkeneIdentificatie.innRechtsvorm)
        assertEquals("bezoekadres", betrokkeneIdentificatie.bezoekadres)
    }

    @Test
    fun `should send outbox message on creating zaakrol`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val responseBody = """
            {
              "url": "https://example.com/rol/d31cd83f-11da-4932-bde8-a9123c9821d3",
              "uuid": "d31cd83f-11da-4932-bde8-a9123c9821d3",
              "zaak": "https://example.com/zaak",
              "betrokkene": "https://example.com/betrokkene",
              "betrokkeneType": "niet_natuurlijk_persoon",
              "roltype": "https://example.com/roltype",
              "omschrijving": "omschrijving",
              "omschrijvingGeneriek": "initiator",
              "roltoelichting": "roltoelichting",
              "registratiedatum": "2019-08-24T14:15:22Z",
              "indicatieMachtiging": "gemachtigde",
              "betrokkeneIdentificatie": {
                "annIdentificatie": "annIdentificatie",
                "innNnpId": "innNnpId",
                "statutaireNaam": "statutaireNaam",
                "innRechtsvorm": "besloten_vennootschap",
                "bezoekadres": "bezoekadres"
              }
            }
        """.trimIndent()

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        mockApi.enqueue(mockResponse(responseBody))

        val result = client.createZaakRol(
            TestAuthentication(),
            URI(mockApi.url("/").toString()),
            Rol(
                zaak = URI("https://example.com/zaak"),
                betrokkeneType = BetrokkeneType.NIET_NATUURLIJK_PERSOON,
                roltype = URI("https://example.com/roltype"),
                roltoelichting = "test",
                betrokkeneIdentificatie = RolNietNatuurlijkPersoon(
                    annIdentificatie = "annIdentificatie"
                )
            )
        )

        mockApi.takeRequest()

        verify(outboxService).send(eventCapture.capture())

        val firstEventValue = eventCapture.firstValue.get()
        val mappedFirstEventResult: Rol = objectMapper.readValue(firstEventValue.result.toString())


        assertThat(firstEventValue).isInstanceOf(ZaakRolCreated::class.java)
        assertThat(result.url.toString()).isEqualTo(firstEventValue.resultId.toString())
        Assertions.assertThat(result.omschrijving).isEqualTo(mappedFirstEventResult.omschrijving)
    }

    @Test
    fun `should not send outbox message on failing to create zaakrol`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        mockApi.enqueue(mockResponse("").setResponseCode(400))

        assertThrows<HttpClientErrorException> {
            client.createZaakRol(
                TestAuthentication(),
                URI(mockApi.url("/").toString()),
                Rol(
                    zaak = URI("https://example.com/zaak"),
                    betrokkeneType = BetrokkeneType.NIET_NATUURLIJK_PERSOON,
                    roltype = URI("https://example.com/roltype"),
                    roltoelichting = "test",
                    betrokkeneIdentificatie = RolNietNatuurlijkPersoon(
                        annIdentificatie = "annIdentificatie"
                    )
                )
            )
        }

        mockApi.takeRequest()

        verify(outboxService, times(0)).send(eventCapture.capture())
    }

    @Test
    fun `should send outbox message on creating zaak`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val responseBody = """
            {
                "url": "https://example.com",
                "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
                "bronorganisatie": "002564440",
                "zaaktype": "https://example.com",
                "verantwoordelijkeOrganisatie": "002564440",
                "startdatum": "2019-08-24"
            }
        """.trimIndent()

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        mockApi.enqueue(mockResponse(responseBody))

        val result = client.createZaak(
            TestAuthentication(),
            URI(mockApi.url("/").toString()),
            CreateZaakRequest(
                bronorganisatie = Rsin("002564440"),
                zaaktype = URI("https://example.com"),
                startdatum = LocalDate.of(2019, 8, 24),
                verantwoordelijkeOrganisatie = Rsin("002564440")
            )
        )

        mockApi.takeRequest()

        verify(outboxService).send(eventCapture.capture())

        val firstEventValue = eventCapture.firstValue.get()
        val mappedFirstEventResult: CreateZaakResponse = objectMapper.readValue(firstEventValue.result.toString())

        assertThat(firstEventValue).isInstanceOf(ZaakCreated::class.java)
        assertThat(result.url.toString()).isEqualTo(firstEventValue.resultId)
        assertThat(result.bronorganisatie).isEqualTo(mappedFirstEventResult.bronorganisatie)
    }

    @Test
    fun `should send outbox message on creating zaakstatus`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val responseBody = """
            {
                "url": "https://example.com",
                "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
                "zaak": "https://example.com",
                "statustype": "https://example.com",
                "statustoelichting": "test",
                "datumStatusGezet": "2018-07-14T17:45:55.9483536"
            }
        """.trimIndent()

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        mockApi.enqueue(mockResponse(responseBody))

        val result = client.createZaakStatus(
            TestAuthentication(),
            URI(mockApi.url("/").toString()),
            CreateZaakStatusRequest(
                zaak = URI("https://example.com"),
                datumStatusGezet = LocalDateTime.of(2023, 8, 3, 3, 3),
                statustype = URI("https://example.com")
            )
        )

        mockApi.takeRequest()

        verify(outboxService).send(eventCapture.capture())

        val firstEventValue = eventCapture.firstValue.get()
        val mappedFirstEventResult: CreateZaakStatusResponse = objectMapper.readValue(firstEventValue.result.toString())

        assertThat(firstEventValue).isInstanceOf(ZaakStatusCreated::class.java)
        assertThat(result.url.toString()).isEqualTo(firstEventValue.resultId)
        assertThat(result.statustoelichting).isEqualTo(mappedFirstEventResult.statustoelichting)
    }

    @Test
    fun `should not send outbox message on failing to create zaakstatus`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        mockApi.enqueue(mockResponse("").setResponseCode(400))

        assertThrows<HttpClientErrorException> {
            client.createZaakStatus(
                TestAuthentication(),
                URI(mockApi.url("/").toString()),
                CreateZaakStatusRequest(
                    zaak = URI("https://example.com"),
                    datumStatusGezet = LocalDateTime.of(2023, 8, 3, 3, 3),
                    statustype = URI("https://example.com")
                )
            )
        }

        mockApi.takeRequest()

        verify(outboxService, times(0)).send(eventCapture.capture())
    }

    @Test
    fun `should send outbox message on creating zaakresultaat`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val responseBody = """
            {
                "url": "https://example.com",
                "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
                "zaak": "https://example.com",
                "resultaattype": "https://example.com",
                "toelichting": "test"
            }
        """.trimIndent()

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        mockApi.enqueue(mockResponse(responseBody))

        val result = client.createZaakResultaat(
            TestAuthentication(),
            URI(mockApi.url("/").toString()),
            CreateZaakResultaatRequest(
                zaak = URI("https://example.com"),
                resultaattype = URI("https://example.com"),
            )
        )

        mockApi.takeRequest()

        verify(outboxService).send(eventCapture.capture())

        val firstEventValue = eventCapture.firstValue.get()
        val mappedFirstEventResult: CreateZaakResultaatResponse =
            objectMapper.readValue(firstEventValue.result.toString())

        assertThat(firstEventValue).isInstanceOf(ZaakResultaatCreated::class.java)
        assertThat(result.url.toString()).isEqualTo(firstEventValue.resultId)
        assertThat(result.toelichting).isEqualTo(mappedFirstEventResult.toelichting)
    }

    @Test
    fun `should not send outbox message on failing to create zaakresultaat`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        mockApi.enqueue(mockResponse("").setResponseCode(400))

        assertThrows<HttpClientErrorException> {
            client.createZaakResultaat(
                TestAuthentication(),
                URI(mockApi.url("/").toString()),
                CreateZaakResultaatRequest(
                    zaak = URI("https://example.com"),
                    resultaattype = URI("https://example.com"),
                )
            )
        }

        mockApi.takeRequest()

        verify(outboxService, times(0)).send(eventCapture.capture())
    }

    @Test
    fun `should send outbox message on setting zaak opschorting`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val responseBody = """
            {
                "url": "https://example.com",
                "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
                "bronorganisatie": "002564440",
                "zaaktype": "https://example.com",
                "verantwoordelijkeOrganisatie": "002564440",
                "omschrijving": "test",
                "toelichting": "test",
                "registratiedatum": "2019-08-24",
                "startdatum": "2019-08-24",
                "communicatiekanaal": "test",
                "identificatie": "test",
                "productenOfDiensten": ["test"],
                "vertrouwelijkheidaanduiding": "test",
                "betalingsindicatie": "test",
                "betalingsindicatieWeergave": "test",
                "selectielijstklasse": "test",
                "deelzaken": ["test"],
                "relevanteAndereZaken": ["test"],
                "eigenschappen": ["test"],
                "kenmerken": ["test"],
                "archiefstatus": "test",
                "opdrachtgevendeOrganisatie": "002564440",
                "opschorting": {
                    "indicatie": true,
                    "reden": "test"
                }
            }
        """.trimIndent()

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        mockApi.enqueue(mockResponse(responseBody))

        val result = client.setZaakOpschorting(
            TestAuthentication(),
            URI(mockApi.url("/").toString()),
            ZaakopschortingRequest(
                verlenging = Verlenging(
                    reden = "test",
                    duur = "test"
                ),
                opschorting = Opschorting(
                    indicatie = true,
                    reden = "test"
                )
            )
        )

        mockApi.takeRequest()

        verify(outboxService).send(eventCapture.capture())

        val firstEventValue = eventCapture.firstValue.get()
        val mappedFirstEventResult: ZaakopschortingResponse = objectMapper.readValue(firstEventValue.result.toString())

        assertThat(firstEventValue).isInstanceOf(ZaakOpschortingUpdated::class.java)
        Assertions.assertThat(result.url).isEqualTo(firstEventValue.resultId)
        Assertions.assertThat(result.opschorting?.reden).isEqualTo(mappedFirstEventResult.opschorting?.reden)
    }

    @Test
    fun `should not send outbox message on failing to set zaak opschorting`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        mockApi.enqueue(mockResponse("").setResponseCode(400))

        assertThrows<HttpClientErrorException> {
            client.setZaakOpschorting(
                TestAuthentication(),
                URI(mockApi.url("/").toString()),
                ZaakopschortingRequest(
                    verlenging = Verlenging(
                        reden = "test",
                        duur = "test"
                    ),
                    opschorting = Opschorting(
                        indicatie = true,
                        reden = "test"
                    )
                )
            )
        }

        mockApi.takeRequest()

        verify(outboxService, times(0)).send(eventCapture.capture())
    }

    @Test
    fun `should send outbox message on retrieving zaak`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val responseBody = """
            {
                "url": "https://example.com",
                "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
                "bronorganisatie": "002564440",
                "zaaktype": "https://example.com",
                "verantwoordelijkeOrganisatie": "002564440",
                "omschrijving": "test",
                "toelichting": "test",
                "registratiedatum": "2019-08-24",
                "startdatum": "2019-08-24",
                "communicatiekanaal": "test",
                "identificatie": "test",
                "productenOfDiensten": ["test"],
                "betalingsindicatie": "test",
                "betalingsindicatieWeergave": "test",
                "selectielijstklasse": "test",
                "deelzaken": ["test"],
                "relevanteAndereZaken": [{"url": "https://example.com", "aardRelatie": "test"}],
                "eigenschappen": ["test"],
                "kenmerken": [{"kenmerk": "test", "bron": "test"}],
                "archiefstatus": "gearchiveerd",
                "opdrachtgevendeOrganisatie": "002564440",
                "vertrouwelijkheidaanduiding": "intern",
                "opschorting": {
                    "indicatie": true,
                    "reden": "test"
                }
            }
        """.trimIndent()

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        mockApi.enqueue(mockResponse(responseBody))

        val result = client.getZaak(
            TestAuthentication(),
            URI(mockApi.url("/").toString()),
        )

        mockApi.takeRequest()

        verify(outboxService).send(eventCapture.capture())

        val firstEventValue = eventCapture.firstValue.get()
        val mappedFirstEventResult: ZaakResponse = objectMapper.readValue(firstEventValue.result.toString())

        assertThat(firstEventValue).isInstanceOf(ZaakViewed::class.java)
        assertThat(result.url.toString()).isEqualTo(firstEventValue.resultId)
        assertThat(result.toelichting).isEqualTo(mappedFirstEventResult.toelichting)
    }

    @Test
    fun `should not send outbox message on failing to retrieve zaak`() {
        val restClientBuilder = RestClient.builder()
        val client = ZakenApiClient(restClientBuilder, outboxService, objectMapper)

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()

        mockApi.enqueue(mockResponse("").setResponseCode(400))

        assertThrows<HttpClientErrorException> {
            client.getZaak(
                TestAuthentication(),
                URI(mockApi.url("/").toString()),
            )
        }

        mockApi.takeRequest()

        verify(outboxService, times(0)).send(eventCapture.capture())
    }

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

    class TestAuthentication : ZakenApiAuthentication {
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