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
import com.ritense.zgw.Rsin
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.camunda.bpm.engine.RepositoryService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doCallRealMethod
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.POST
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClient
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import java.lang.Thread.sleep
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals

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
        val mockedId = PluginConfigurationId.existingId(UUID.fromString("27a399c7-9d70-4833-a651-57664e2e9e09"))
        doReturn(Optional.of(mock<PluginConfiguration>())).whenever(pluginConfigurationRepository).findById(mockedId)
        doReturn(TestAuthentication()).whenever(pluginService).createInstance(mockedId)
        doCallRealMethod().whenever(pluginService).createPluginConfiguration(any(), any(), any())

        // Setting up plugin
        val pluginPropertiesJson = """
            {
              "url": "${server.url("/")}",
              "authenticationPluginConfiguration": "27a399c7-9d70-4833-a651-57664e2e9e09"
            }
        """.trimIndent()

        val configuration = pluginService.createPluginConfiguration(
            "Zaken API plugin configuration",
            objectMapper.readTree(
                pluginPropertiesJson
            ) as ObjectNode,
            "zakenapi"
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
        val zakenApiPlugin = pluginService.createInstance<ZakenApiPlugin>(UUID.fromString(ZAKEN_API_PLUGIN_ID))
        val document = runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(DOCUMENT_DEFINITION_KEY, objectMapper.createObjectNode())
            ).resultingDocument().get()
        }

        zakenApiPlugin.createZaak(
            document.id().id,
            Rsin("155539620"),
            URI("http://localhost:56273/catalogi/my-zaaktype-id")
        )

        val requestBody = getRequestBody(POST, "/zaken/zaken", CreateZaakRequest::class.java)
        assertEquals(requestBody.uiterlijkeEinddatumAfdoening, LocalDate.now().plusDays(84))
    }

    @Test
    fun `should create zaak with description and plannedEndDate`() {
        val zakenApiPlugin = pluginService.createInstance<ZakenApiPlugin>(UUID.fromString(ZAKEN_API_PLUGIN_ID))
        val document = runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(DOCUMENT_DEFINITION_KEY, objectMapper.createObjectNode())
            ).resultingDocument().get()
        }
        val description = "omschrijving"
        val plannedEndDate = LocalDate.now().plusDays(10)

        zakenApiPlugin.createZaak(
            document.id().id,
            Rsin("155539620"),
            URI("http://localhost:56273/catalogi/my-zaaktype-id"),
            description,
            plannedEndDate,
            null
        )

        val requestBody = getRequestBody(POST, "/zaken/zaken", CreateZaakRequest::class.java)
        assertEquals(requestBody.omschrijving, description)
        assertEquals(requestBody.einddatumGepland, plannedEndDate)
    }

    @Test
    fun `should link document to zaak`() {
        val newDocumentRequest = NewDocumentRequest(DOCUMENT_DEFINITION_KEY, objectMapper.createObjectNode())
        val request = NewDocumentAndStartProcessRequest(PROCESS_DEFINITION_KEY, newDocumentRequest)

        // Make a record in the database about a document that is matched to the open zaak
        val resource = mock<Resource>()
        whenever(resource.id()).thenReturn(UUID.randomUUID())
        whenever(resource.name()).thenReturn("name")
        whenever(resource.sizeInBytes()).thenReturn(1L)
        whenever(resource.extension()).thenReturn("ext")
        whenever(resource.createdOn()).thenReturn(LocalDateTime.now())

        whenever(resourceService.getResource(resource.id())).thenReturn(resource)
        whenever(resourceProvider.getResource(any())).thenReturn(resource)

        // Start the process
        val response = runWithoutAuthorization { procesDocumentService.newDocumentAndStartProcess(request) }
        assertTrue(response is NewDocumentAndStartProcessResultSucceeded)

        // Check the request that was sent to the open zaak api
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
        val newDocumentRequest = NewDocumentRequest(DOCUMENT_DEFINITION_KEY, objectMapper.createObjectNode())
        val request = NewDocumentAndStartProcessRequest(PROCESS_DEFINITION_KEY, newDocumentRequest)

        // Make a record in1 the database about a document that is matched to the open zaak
        val resource = mock<Resource>()
        whenever(resource.id()).thenReturn(UUID.randomUUID())
        whenever(resource.name()).thenReturn("name")
        whenever(resource.sizeInBytes()).thenReturn(1L)
        whenever(resource.extension()).thenReturn("ext")
        whenever(resource.createdOn()).thenReturn(LocalDateTime.now())

        whenever(resourceService.getResource(resource.id())).thenReturn(resource)
        whenever(resourceProvider.getResource(any())).thenReturn(resource)

        // Start the process
        val response = runWithoutAuthorization { procesDocumentService.newDocumentAndStartProcess(request) }
        assertTrue(response is NewDocumentAndStartProcessResultSucceeded)

        // Check the request that was sent to the open zaak api
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

    private fun setupMockZakenApiServer() {
        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                executedRequests.add(request)
                val path = request.path?.substringBefore('?')
                val response = when (path) {
                    "/zaakinformatieobjecten" -> handleZaakInformatieObjectRequest()
                    "/catalogi/my-zaaktype-id" -> getZaaktypeResponse()
                    "/zaken/zaken" -> createZaakResponse()
                    "/catalogi/informatieobjecttypen?status=definitief&page=1" -> MockResponse().setResponseCode(200)
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
              "zaak": "http://example.com",
              "aardRelatieWeergave": "Hoort bij, omgekeerd: kent",
              "titel": "string",
              "beschrijving": "string",
              "registratiedatum": "2019-08-24T14:15:22Z"
            }
        """.trimIndent()
        return mockResponse(body)
    }

    private fun createZaakResponse(): MockResponse {
        val body = """
            {
                "url": "http://localhost/zaken/api/v1/zaken/95b9a6a8-978c-40f7-93d0-eb4b46597355",
                "uuid": "95b9a6a8-978c-40f7-93d0-eb4b46597355",
                "identificatie": "ZAAK-2023-0000000001",
                "bronorganisatie": "419071349",
                "omschrijving": "",
                "toelichting": "",
                "zaaktype": "http://localhost/catalogi/api/v1/zaaktypen/744ca059-f412-49d4-8963-5800e4afd486",
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

    private fun getZaaktypeResponse(): MockResponse {
        val body = """
            {
                "url": "http://localhost/catalogi/api/v1/zaaktypen/744ca059-f412-49d4-8963-5800e4afd486",
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
        private val ZAAK_URL = URI("http://localhost:56273/zaken/57f66ff6-db7f-43bc-84ef-6847640d3609")
    }
}
