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

package com.ritense.zakenapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.catalogiapi.CatalogiApiAuthentication
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.plugin.domain.PluginProcessLinkId
import com.ritense.plugin.repository.PluginProcessLinkRepository
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processdocument.service.impl.result.NewDocumentAndStartProcessResultSucceeded
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.valtimo.contract.resource.Resource
import com.ritense.zakenapi.domain.CreateZaakRequest
import com.ritense.zakenapi.domain.PatchZaakRequest
import com.ritense.zakenapi.domain.zaakobjectrequest.SimpleZaakObjectRequest
import com.ritense.zakenapi.domain.zaakobjectrequest.ZaakObjectOverigeRequest
import com.ritense.zakenapi.domain.zaakobjectrequest.ZaakObjectRequest
import com.ritense.zakenapi.domain.zaakobjectrequest.ZaakObjectType
import com.ritense.zakenapi.domain.zaakobjectrequest.ZaakObjectZakelijkRechtRequest
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import com.ritense.zgw.Rsin
import java.lang.Thread.sleep
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doCallRealMethod
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.operaton.bpm.engine.RepositoryService
import org.operaton.bpm.engine.delegate.DelegateExecution
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.PATCH
import org.springframework.http.HttpMethod.POST
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClient
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono

@Transactional
class ZakenApiPluginIT : BaseIntegrationTest() {

    @Autowired
    lateinit var repositoryService: RepositoryService

    @Autowired
    lateinit var pluginProcessLinkRepository: PluginProcessLinkRepository

    @Autowired
    lateinit var procesDocumentService: ProcessDocumentService

    @Autowired
    lateinit var documentService: DocumentService

    @Autowired
    lateinit var zaakInstanceLinkService: ZaakInstanceLinkService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    lateinit var server: MockWebServer

    lateinit var processDefinitionId: String

    private var executedRequests: MutableList<RecordedRequest> = mutableListOf()

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockZakenApiServer()
        server.start(port = 56273)
        sleep(2000) // Needed to fix connection refused error

        // Since we do not have an actual authentication plugin in this context we will mock one
        val mockedId = PluginConfigurationId.existingId(UUID.fromString(AUTHENTICATION_PLUGIN_ID))
        doReturn(Optional.of(mock<PluginConfiguration>()))
            .whenever(pluginConfigurationRepository).findById(mockedId)
        doReturn(TestAuthentication())
            .whenever(pluginService).createInstance(mockedId)
        doCallRealMethod()
            .whenever(pluginService).createPluginConfiguration(any(), any(), any())

        // Setting up plugin
        val pluginPropertiesJson = """
            {
              "url": "${server.url("${ZAKEN_API_PATH}/")}",
              "authenticationPluginConfiguration": "$AUTHENTICATION_PLUGIN_ID"
            }
        """.trimIndent()

        val configuration = pluginService.createPluginConfiguration(
            title = "Zaken API plugin configuration",
            properties = objectMapper.readTree(
                pluginPropertiesJson
            ) as ObjectNode,
            pluginDefinitionKey = "zakenapi"
        )

        val actionPropertiesJson = """
            {
                "documentUrl" : "$INFORMATIE_OBJECT_URL",
                "titel": "titelVariableName",
                "beschrijving": "beschrijvingVariableName"
            }
        """.trimIndent()

        processDefinitionId = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey("zaken-api-plugin")
            .latestVersion()
            .singleResult()
            .id

        pluginProcessLinkRepository.save(
            PluginProcessLink(
                PluginProcessLinkId(UUID.randomUUID()),
                processDefinitionId,
                "LinkDocument",
                objectMapper.readTree(actionPropertiesJson) as ObjectNode,
                configuration.id,
                "link-document-to-zaak",
                ActivityTypeWithEventName.SERVICE_TASK_START
            )
        )
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should create zaak with uiterlijkeEinddatumAfdoening`() {
        val zakenApiPlugin = zakenApiPlugin()
        val document = createDocument()

        zakenApiPlugin.createZaak(
            documentId = document.id().id,
            rsin = Rsin("155539620"),
            zaaktypeUrl = ZAAKTYPE_URL
        )

        val requestBody = createZaakRequestBody()
        assertEquals(requestBody.uiterlijkeEinddatumAfdoening, LocalDate.now().plusDays(84))
    }

    @Test
    fun `should create zaak with description and plannedEndDate`() {
        val zakenApiPlugin = zakenApiPlugin()
        val document = createDocument()
        val description = "omschrijving"
        val plannedEndDate = LocalDate.now().plusDays(10)

        zakenApiPlugin.createZaak(
            documentId = document.id().id,
            rsin = Rsin("155539620"),
            zaaktypeUrl = ZAAKTYPE_URL,
            description = description,
            plannedEndDate = plannedEndDate,
            finalDeliveryDate = null
        )

        val requestBody = createZaakRequestBody()
        assertEquals(requestBody.omschrijving, description)
        assertEquals(requestBody.einddatumGepland, plannedEndDate)
    }

    private fun createZaakRequestBody() =
        getRequestBody(POST, "${ZAKEN_API_PATH}/zaken", CreateZaakRequest::class.java)

    @Test
    fun `should patch zaak with description and finalDeliveryDate`() {
        // given
        val zakenApiPlugin = zakenApiPlugin()
        val document = createDocument()

        zakenApiPlugin.createZaak(
            documentId = document.id().id,
            rsin = Rsin("155539620"),
            zaaktypeUrl = ZAAKTYPE_URL,
        )

        val description = "omschrijving na patch"
        val finalDeliveryDate = LocalDate.now().plusDays(10)

        // when
        zakenApiPlugin.patchZaak(
            documentId = document.id().id,
            description = description,
            finalDeliveryDate = finalDeliveryDate
        )

        val requestBody = getRequestBody(PATCH, "${ZAKEN_API_PATH}/zaken/$ZAAK_ID", PatchZaakRequest::class.java)
        assertEquals(requestBody.omschrijving, description)
        assertEquals(requestBody.uiterlijkeEinddatumAfdoening, finalDeliveryDate)
    }

    @Test
    fun `should link document to zaak`() {
        val newDocumentRequest = newDocumentRequest()
        val request = NewDocumentAndStartProcessRequest(PROCESS_DEFINITION_KEY, newDocumentRequest)

        // Make a record in the database about a document that is matched to the open zaak
        setupResourceMock()

        // Start the process
        val response = runWithoutAuthorization { procesDocumentService.newDocumentAndStartProcess(request) }
        assertTrue(response is NewDocumentAndStartProcessResultSucceeded)

        // Check the request that was sent to the open zaak api
        server.takeRequest()
        val recordedRequest = server.takeRequest()
        val requestString = recordedRequest.body.readUtf8()
        val parsedOutput = objectMapper.readValue(requestString, Map::class.java)

        assertEquals(4, parsedOutput.size)
        assertEquals(INFORMATIE_OBJECT_URL, parsedOutput["informatieobject"])
        assertEquals(ZAAK_URL.toString(), parsedOutput["zaak"])
        assertEquals("titelVariableName", parsedOutput["titel"])
        assertEquals("beschrijvingVariableName", parsedOutput["beschrijving"])

        // Check to see if the document is correctly linked inside the valtimo database as well
        assertNotNull(response.resultingDocument())
        assertTrue(response.resultingDocument().isPresent)
        val processDocumentId = response.resultingDocument().get().id().id
        assertNotNull(runWithoutAuthorization { documentService.get(processDocumentId.toString()) })
    }

    @Test
    fun `should link uploaded document to zaak`() {
        val newDocumentRequest = newDocumentRequest()
        val request = NewDocumentAndStartProcessRequest(PROCESS_DEFINITION_KEY, newDocumentRequest)

        // Make a record in1 the database about a document that is matched to the open zaak
        setupResourceMock()

        // Start the process
        val response = runWithoutAuthorization { procesDocumentService.newDocumentAndStartProcess(request) }
        assertTrue(response is NewDocumentAndStartProcessResultSucceeded)

        // Check the request that was sent to the open zaak api
        server.takeRequest()
        val recordedRequest = server.takeRequest()
        val requestString = recordedRequest.body.readUtf8()
        val parsedOutput = objectMapper.readValue(requestString, Map::class.java)

        assertEquals(4, parsedOutput.size)
        assertEquals(INFORMATIE_OBJECT_URL, parsedOutput["informatieobject"])
        assertEquals(ZAAK_URL.toString(), parsedOutput["zaak"])
        assertEquals("titelVariableName", parsedOutput["titel"])
        assertEquals("beschrijvingVariableName", parsedOutput["beschrijving"])

        // Check to see if the document is correctly linked inside the valtimo database as well
        assertNotNull(response.resultingDocument())
        assertTrue(response.resultingDocument().isPresent)
        val processDocumentId = response.resultingDocument().get().id().id
        assertNotNull(runWithoutAuthorization { documentService.get(processDocumentId.toString()) })
    }

    @Test
    fun `should create zaak object`() {
        val zakenApiPlugin = pluginService.createInstance<ZakenApiPlugin>(UUID.fromString(ZAKEN_API_PLUGIN_ID))

        val zaakUrl = URI("http://localhost:56273/zaken/41e90cab-7f81-4a45-883d-430b7a6d9900")
        val objectUrl = URI("")
        val zaakobjecttype = "http://localhost:56273/catalogi/my-zaaktype-id"
        val objectType = ZaakObjectType.ADRES
        val relatieomschrijving = ""

        zakenApiPlugin.createZaakObject(
            null,
            SimpleZaakObjectRequest(
                zaakUrl = zaakUrl,
                objectUrl = objectUrl,
                zaakobjecttype = zaakobjecttype,
                objectType = objectType,
                relatieomschrijving = relatieomschrijving
            )
        )

        val requestBody = getRequestBody(POST, "${ZAKEN_API_PATH}/zaakobjecten", ZaakObjectRequest::class.java)
        assertThat(requestBody.zaakUrl).isEqualTo(zaakUrl)
        assertThat(requestBody.objectUrl).isEqualTo(objectUrl)
        assertThat(requestBody.zaakobjecttype).isEqualTo(zaakobjecttype)
        assertThat(requestBody.objectType).isEqualTo(objectType)
        assertThat(requestBody.relatieomschrijving).isEqualTo(relatieomschrijving)
    }

    @Test
    fun `should create zaak object via legacy function`() {
        val zakenApiPlugin = pluginService.createInstance<ZakenApiPlugin>(UUID.fromString(ZAKEN_API_PLUGIN_ID))

        val zaakUrl = URI("http://localhost:56273/zaken/41e90cab-7f81-4a45-883d-430b7a6d9900")
        val objectUrl = URI("")
        val objectType = ZaakObjectType.OVERIGE
        val objectTypeOverige = "zaakdetails"

        zakenApiPlugin.createZaakObject(
            zaakUrl = zaakUrl,
            objectUrl = objectUrl,
            objectTypeOverige = objectTypeOverige,
            UUID.randomUUID()
        )

        val requestBody = getRequestBody(POST, "${ZAKEN_API_PATH}/zaakobjecten", ZaakObjectOverigeRequest::class.java)
        assertThat(requestBody.zaakUrl).isEqualTo(zaakUrl)
        assertThat(requestBody.objectUrl).isEqualTo(objectUrl)
        assertThat(requestBody.objectType).isEqualTo(objectType)
        assertThat(requestBody.objectTypeOverige).isEqualTo(objectTypeOverige)
    }

    @Test
    fun `should resolve values when creating zaak object`() {
        val zakenApiPlugin = pluginService.createInstance<ZakenApiPlugin>(UUID.fromString(ZAKEN_API_PLUGIN_ID))

        val edossierNummer = "E.123.4"
        val zaakUrl = URI("http://localhost:56273/zaken/123")
        val relatieomschrijving = "Betrokken erfpachtdossier"
        val identificatie = "doc:verzoek.metaData.eDossiernummer"
        val avgAard = "Erfpacht"

        val document = runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    DOCUMENT_DEFINITION_KEY,
                    "profile",
                    "1.0.0",
                    objectMapper.readTree("""
                        {
                            "verzoek": {
                                "metaData": {
                                    "eDossiernummer": "${edossierNummer}"
                                }
                            }
                        }
                    """.trimIndent())
                )
            ).resultingDocument().get()
        }

        zaakInstanceLinkService.createZaakInstanceLink(
            zaakUrl,
            UUID.randomUUID(),
            document.id().id,
            URI("http://localhost:56273/zaak-type/456")
        )

        val execution: DelegateExecution = mock()
        val processInstanceId = UUID.randomUUID().toString()

        doReturn(document.id().id.toString()).whenever(execution).businessKey
        doReturn(processInstanceId).whenever(execution).processInstanceId

        runWithoutAuthorization {
            zakenApiPlugin.createZaakObject(
                execution,
                ZaakObjectZakelijkRechtRequest(
                    zaakUrl = null,
                    relatieomschrijving = relatieomschrijving,
                    objectIdentificatie = ZaakObjectZakelijkRechtRequest.ZakelijkRechtIdentificatie(
                        identificatie = identificatie,
                        avgAard = avgAard
                    )
                )
            )
        }

        val requestBody = getRequestBody(POST, "${ZAKEN_API_PATH}/zaakobjecten", ZaakObjectZakelijkRechtRequest::class.java)
        assertThat(requestBody.zaakUrl).isEqualTo(zaakUrl)
        assertThat(requestBody.objectType).isEqualTo(ZaakObjectType.ZAKELIJK_RECHT)
        assertThat(requestBody.relatieomschrijving).isEqualTo(relatieomschrijving)
        assertThat(requestBody.objectIdentificatie?.identificatie).isEqualTo(edossierNummer)
        assertThat(requestBody.objectIdentificatie?.avgAard).isEqualTo(avgAard)
    }

    private fun setupResourceMock() {
        val resource = mock<Resource>()
        whenever(resource.id())
            .thenReturn(UUID.randomUUID())
        whenever(resource.name())
            .thenReturn("name")
        whenever(resource.sizeInBytes())
            .thenReturn(1L)
        whenever(resource.extension())
            .thenReturn("ext")
        whenever(resource.createdOn())
            .thenReturn(LocalDateTime.now())

        whenever(resourceService.getResource(eq(resource.id())))
            .thenReturn(resource)
        whenever(resourceProvider.getResource(any()))
            .thenReturn(resource)
    }

    private fun zakenApiPlugin() =
        pluginService.createInstance<ZakenApiPlugin>(UUID.fromString(ZAKEN_API_PLUGIN_ID))

    private fun createDocument(): Document =
        runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    DOCUMENT_DEFINITION_KEY,
                    "profile",
                    "1.0.0",
                    objectMapper.createObjectNode()
                )
            ).resultingDocument().get()
        }

    private fun newDocumentRequest() = NewDocumentRequest(
        DOCUMENT_DEFINITION_KEY,
        "profile",
        "1.0.0",
        objectMapper.createObjectNode()
    )

    private fun setupMockZakenApiServer() {
        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                executedRequests.add(request)
                val response = when (request.method + " " + request.path?.substringBefore('?')) {
                    "GET ${CATALOGI_API_PATH}/zaaktypen/${ZAAKTYPE_ID}" -> zaaktypeResponse()
                    "GET ${CATALOGI_API_PATH}/informatieobjecttypen" -> MockResponse().setResponseCode(200)
                    "POST ${ZAKEN_API_PATH}/zaakinformatieobjecten" -> handleZaakInformatieObjectRequest()
                    "GET ${ZAKEN_API_PATH}/zaakinformatieobjecten" -> mockResponse("[]")
                    "POST ${ZAKEN_API_PATH}/zaken" -> zaakResponse()
                    "PATCH ${ZAKEN_API_PATH}/zaken/${ZAAK_ID}" -> zaakResponse()
                    "POST ${ZAKEN_API_PATH}/zaakobjecten" -> createZaakObjectResponse()
                    else -> MockResponse().setResponseCode(404)
                }
                return response
            }
        }

        server.dispatcher = dispatcher
    }

    private fun handleZaakInformatieObjectRequest(): MockResponse {
        val body = """
            {
              "url": "http://example.com",
              "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
              "informatieobject": "$INFORMATIE_OBJECT_URL",
              "zaak": "$ZAAK_URL",
              "aardRelatieWeergave": "Hoort bij, omgekeerd: kent",
              "titel": "string",
              "beschrijving": "string",
              "registratiedatum": "2019-08-24T14:15:22Z"
            }
        """.trimIndent()
        return mockResponse(body)
    }

    private fun zaakResponse(): MockResponse {
        val body = """
            {
                "url": "$ZAAK_URL",
                "uuid": "$ZAAK_ID",
                "identificatie": "ZAAK-2023-0000000001",
                "bronorganisatie": "419071349",
                "omschrijving": "",
                "toelichting": "",
                "zaaktype": "$ZAAKTYPE_URL",
                "registratiedatum": "2024-02-13",
                "verantwoordelijkeOrganisatie": "420936440",
                "startdatum": "2023-01-23",
                "einddatum": null,
                "einddatumGepland": null,
                "uiterlijkeEinddatumAfdoening": null,
                "publicatiedatum": null,
                "communicatiekanaal": "",
                "productenOfDiensten": [],
                "vertrouwelijkheidaanduiding": "zaakvertrouwelijk",
                "betalingsindicatie": "",
                "betalingsindicatieWeergave": "",
                "laatsteBetaaldatum": null,
                "zaakgeometrie": null,
                "verlenging": null,
                "opschorting": {
                    "indicatie": false,
                    "reden": ""
                },
                "selectielijstklasse": "",
                "hoofdzaak": null,
                "deelzaken": [],
                "relevanteAndereZaken": [],
                "eigenschappen": [],
                "status": null,
                "kenmerken": [],
                "archiefnominatie": null,
                "archiefstatus": "nog_te_archiveren",
                "archiefactiedatum": null,
                "resultaat": null,
                "opdrachtgevendeOrganisatie": ""
            }
        """.trimIndent()
        return mockResponse(body)
    }

    private fun createZaakObjectResponse(): MockResponse {
        val body = """
            {
                "url": "http://example.com",
                "uuid": "ffa06285-d60f-4748-8fcf-15a93c5fb308",
                "zaak": "http://localhost:56273/zaken/41e90cab-7f81-4a45-883d-430b7a6d9900",
                "object": "",
                "zaakobjecttype": "http://localhost:56273/catalogi/my-zaaktype-id",
                "objectType": "adres",
                "objectTypeOverige": "a",
                "objectTypeOverigeDefinitie": {
                    "url": "http://example.com",
                    "schema": "string",
                    "objectData": "string"
                },
                "relatieomschrijving": "string",
                "_expand": {
                    "zaakobjecttype": {}
                },
                "objectIdentificatie": {
                    "identificatie": "string",
                    "wplWoonplaatsNaam": "string",
                    "gorOpenbareRuimteNaam": "string",
                    "huisnummer": 99999,
                    "huisletter": "s",
                    "huisnummertoevoeging": "stri",
                    "postcode": "string"
                }
            }
        """.trimIndent()
        return mockResponse(body).setResponseCode(201)
    }

    private fun zaaktypeResponse(): MockResponse {
        val body = """
            {
                "url": "$ZAAKTYPE_URL",
                "identificatie": "example-case",
                "omschrijving": "Example case",
                "omschrijvingGeneriek": "Example case",
                "vertrouwelijkheidaanduiding": "zaakvertrouwelijk",
                "doel": "For test purposes.",
                "aanleiding": "aanleiding",
                "toelichting": "toelichting",
                "indicatieInternOfExtern": "extern",
                "handelingInitiator": "Indienen",
                "onderwerp": "Example",
                "handelingBehandelaar": "Case",
                "doorlooptijd": "P84D",
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
                "catalogus": "${CATALOGI_API_URL}catalogussen/8225508a-6840-413e-acc9-6422af120db1",
                "statustypen": [
                    "${CATALOGI_API_URL}statustypen/12345678-3f25-4716-5432-49ea8e954fd0"
                ],
                "resultaattypen": [],
                "eigenschappen": [
                    "${CATALOGI_API_URL}eigenschappen/12345678-b04b-424b-ab02-c4102b562633"
                ],
                "informatieobjecttypen": [
                    "${CATALOGI_API_URL}informatieobjecttypen/12345678-be3b-4bad-9e3c-49a6219c92ad"
                ],
                "roltypen": [
                    "${CATALOGI_API_URL}roltypen/12345678-c38d-47b8-bed5-994db88ead61"
                ],
                "besluittypen": [],
                "deelzaaktypen": [],
                "gerelateerdeZaaktypen": [],
                "beginGeldigheid": "2021-01-01",
                "eindeGeldigheid": null,
                "versiedatum": "2021-01-01",
                "concept": false
            }
        """.trimIndent()
        return mockResponse(body)
    }

    fun findRequest(method: HttpMethod, path: String): RecordedRequest? {
        return executedRequests
            .filter { method.matches(it.method!!) }
            .firstOrNull { it.path?.substringBefore('?').equals(path) }
    }

    fun <T> getRequestBody(method: HttpMethod, path: String, clazz: Class<T>): T {
        return objectMapper.readValue(findRequest(method, path)!!.body.readUtf8(), clazz)
    }

    class TestAuthentication : ZakenApiAuthentication, CatalogiApiAuthentication {
        override fun applyAuth(builder: RestClient.Builder): RestClient.Builder {
            return builder.defaultHeaders { headers ->
                headers.setBearerAuth("test")
            }
        }

        override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
            return next.exchange(request)
        }
    }

    companion object {
        private const val PROCESS_DEFINITION_KEY = "zaken-api-plugin"
        private const val DOCUMENT_DEFINITION_KEY = "profile"
        private const val INFORMATIE_OBJECT_URL = "http://informatie.object.url"
        private const val ZAKEN_API_PLUGIN_ID = "3079d6fe-42e3-4f8f-a9db-52ce2507b7ee"
        private const val AUTHENTICATION_PLUGIN_ID = "27a399c7-9d70-4833-a651-57664e2e9e09"

        private const val ZAAKTYPE_ID = "21c0946a-9058-11ee-b9d1-0242ac120002"
        private const val ZAAK_ID = "57f66ff6-db7f-43bc-84ef-6847640d3609"

        private const val CATALOGI_API_PATH = "/catalogi/api/v1"
        private const val CATALOGI_API_URL = "http://localhost:56273$CATALOGI_API_PATH"
        private const val ZAKEN_API_PATH = "/zaken/api/v1"
        private const val ZAKEN_API_URL = "http://localhost:56273$ZAKEN_API_PATH"

        private val ZAAKTYPE_URL = URI("${CATALOGI_API_URL}/zaaktypen/$ZAAKTYPE_ID")
        private val ZAAK_URL = URI("${ZAKEN_API_URL}/zaken/$ZAAK_ID")
    }
}
