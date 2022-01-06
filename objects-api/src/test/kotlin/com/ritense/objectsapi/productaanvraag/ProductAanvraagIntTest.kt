/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
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

package com.ritense.objectsapi.productaanvraag

import com.jayway.jsonpath.JsonPath
import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorInstance
import com.ritense.connector.domain.ConnectorInstanceId
import com.ritense.connector.domain.ConnectorType
import com.ritense.connector.repository.ConnectorTypeInstanceRepository
import com.ritense.connector.service.ConnectorDeploymentService
import com.ritense.connector.service.ConnectorService
import com.ritense.klant.domain.Klant
import com.ritense.objectsapi.BaseIntegrationTest
import com.ritense.objectsapi.domain.AbonnementLink
import com.ritense.objectsapi.opennotificaties.OpenNotificatieProperties
import com.ritense.objectsapi.repository.AbonnementLinkRepository
import com.ritense.objectsapi.service.ObjectTypeConfig
import com.ritense.objectsapi.service.ObjectsApiProperties
import com.ritense.objectsapi.service.ServerAuthSpecification
import com.ritense.openzaak.domain.configuration.Rsin
import com.ritense.openzaak.domain.connector.OpenZaakConfig
import com.ritense.openzaak.domain.connector.OpenZaakProperties
import com.ritense.openzaak.domain.mapping.impl.Operation
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLinkId
import com.ritense.openzaak.domain.request.CreateZaakTypeLinkRequest
import com.ritense.openzaak.service.ZaakTypeLinkService
import com.ritense.openzaak.web.rest.request.ServiceTaskHandlerRequest
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.net.URI
import java.util.UUID
import javax.transaction.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

@Transactional
class ProductAanvraagIntTest : BaseIntegrationTest() {

    @Autowired
    @Qualifier("productAanvraagConnector")
    lateinit var productAanvraagConnector: Connector

    @Autowired
    @Qualifier("openZaakConnector")
    lateinit var openZaakConnector: Connector

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    lateinit var connectorDeploymentService: ConnectorDeploymentService

    @Autowired
    lateinit var connectorTypeInstanceRepository: ConnectorTypeInstanceRepository

    @Autowired
    lateinit var connectorService: ConnectorService

    @Autowired
    lateinit var zaakTypeLinkService: ZaakTypeLinkService

    @Autowired
    lateinit var processDocumentAssociationService: ProcessDocumentAssociationService

    @Autowired
    lateinit var abonnementLinkRepository: AbonnementLinkRepository

    lateinit var server: MockWebServer
    lateinit var mockMvc: MockMvc
    lateinit var baseUrl: String

    lateinit var connectorType: ConnectorType
    lateinit var productAanvraagConnectorInstance: ConnectorInstance
    lateinit var openZaakConnectorInstance: ConnectorInstance
    lateinit var abonnementLink: AbonnementLink
    lateinit var executedRequests: MutableList<RecordedRequest>

    lateinit var zaakTypeLinkId: ZaakTypeLinkId

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        executedRequests = mutableListOf()
        setupMockServer()
        server.start()
        baseUrl = server.url("/").toString()
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()

        connectorDeploymentService.deployAll(listOf(productAanvraagConnector, openZaakConnector))
        connectorType = connectorService.getConnectorTypes()
            .filter { it.name.equals("ProductAanvragen") }
            .first()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should create abonnement when creating productaanvraag connector`() {
        val postBody = """
            {
                "typeId": "${connectorType.id.id}",
                "name": "Productaanvragen",
                "connectorProperties":{
                    "className": "com.ritense.objectsapi.productaanvraag.ProductAanvraagProperties",
                    "objectsApiProperties":{
                        "className": "com.ritense.objectsapi.service.ObjectsApiProperties",
                        "objectsApi":{
                            "url": "$baseUrl",
                            "token": "123"
                        },
                        "objectsTypeApi":{
                            "url": "$baseUrl",
                            "token": "456"
                        },
                        "objectType":{
                            "name": "productAanvraag",
                            "title": "Product Aanvraag",
                            "url": "${baseUrl}api/v1/objecttypes/021f685e-9482-4620-b157-34cd4003da6b",
                            "typeversion": "1"
                        }
                    },
                    "openNotificatieProperties":{
                        "baseUrl": "$baseUrl",
                        "clientId": "valtimo",
                        "secret": "33d2f33d-93fe-4351-88bd-8d9b69b8d978",
                        "callbackBaseUrl": "$baseUrl"
                    },
                    "typeMapping": [
                        {
                            "productAanvraagType": "test",
                            "caseDefinitionKey": "test",
                            "processDefinitionKey": "test"
                        }
                    ],
                    "aanvragerRolTypeUrl": "${baseUrl}catalogi/api/v1/roltypen/1c359a1b-c38d-47b8-bed5-994db88ead61"
                }
            }
        """.trimIndent()

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/connector/instance")
                .content(postBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
                .andReturn()

        val id: String = JsonPath.read(result.getResponse().getContentAsString(), "$.connectorTypeInstance.id")
        val abonnementLink = abonnementLinkRepository.findById(ConnectorInstanceId.existingId(UUID.fromString(id)))

        verifyRequestSent(HttpMethod.POST, "/api/v1/abonnement")
        assertTrue(abonnementLink.isPresent)
        assertEquals("fd876e8c-f081-4093-9157-7b21645043dd", abonnementLink.get().abonnementId.toString())
    }

    @Test
    fun `should recreate abonnement when modifying productaanvraag connector`() {
        prepareConnectorInstance()

        val putBody = """
            {
                "id": "${productAanvraagConnectorInstance.id.id}",
                "typeId": "${connectorType.id.id}",
                "name": "Productaanvragen",
                "connectorProperties":{
                    "className": "com.ritense.objectsapi.productaanvraag.ProductAanvraagProperties",
                    "objectsApiProperties":{
                        "className": "com.ritense.objectsapi.service.ObjectsApiProperties",
                        "objectsApi":{
                            "url": "$baseUrl",
                            "token": "123"
                        },
                        "objectsTypeApi":{
                            "url": "$baseUrl",
                            "token": "456"
                        },
                        "objectType":{
                            "name": "productAanvraag",
                            "title": "Product Aanvraag",
                            "url": "${baseUrl}api/v1/objecttypes/021f685e-9482-4620-b157-34cd4003da6b",
                            "typeversion": "1"
                        }
                    },
                    "openNotificatieProperties":{
                        "baseUrl": "$baseUrl",
                        "clientId": "valtimo",
                        "secret": "33d2f33d-93fe-4351-88bd-8d9b69b8d978",
                        "callbackBaseUrl": "$baseUrl"
                    },
                    "typeMapping": [
                        {
                            "productAanvraagType": "test",
                            "caseDefinitionKey": "test",
                            "processDefinitionKey": "test"
                        }
                    ],
                    "aanvragerRolTypeUrl": "${baseUrl}catalogi/api/v1/roltypen/1c359a1b-c38d-47b8-bed5-994db88ead61"
                }
            }
        """.trimIndent()

        val result = mockMvc.perform(
            MockMvcRequestBuilders.put("/api/connector/instance")
                .content(putBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
                .andReturn()

        verifyRequestSent(HttpMethod.DELETE, "/api/v1/abonnement/693fb25a-20ee-407b-917b-9dd0e76988c9")
        verifyRequestSent(HttpMethod.POST, "/api/v1/abonnement")

        val id: String = JsonPath.read(result.getResponse().getContentAsString(), "$.connectorTypeInstance.id")
        val abonnementLink = abonnementLinkRepository.findById(ConnectorInstanceId.existingId(UUID.fromString(id)))

        assertTrue(abonnementLink.isPresent)
        assertEquals("fd876e8c-f081-4093-9157-7b21645043dd", abonnementLink.get().abonnementId.toString())
    }

    @Test
    fun `should delete abonnement when deleting productaanvraag connector`() {
        prepareConnectorInstance()

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/connector/instance/${productAanvraagConnectorInstance.id.id}")
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

        verifyRequestSent(HttpMethod.DELETE, "/api/v1/abonnement/693fb25a-20ee-407b-917b-9dd0e76988c9")
        val abonnementLinks = abonnementLinkRepository.findAll()
        assertEquals(0, abonnementLinks.size)
    }

    @Test
    fun `should handle ProductAanvraagRequest`() {
        prepareConnectorInstance()
        prepareOpenZaakConfig()
        prepareDocumentDefinitionSettings()

        `when`(burgerService.ensureBurgerExists("051845623")).thenReturn(Klant(
            "http://www.example.com/some-id",
            "0123456789",
            "test@example.com"
        ))

        val postBody = """
            {
                "kanaal": "objecten",
                "hoofdObject": "http://localhost:8000/api/v2/objects/7d5f985a-a0c4-4b4b-8550-2be98160e777",
                "resource": "object",
                "resourceUrl": "http://localhost:8000/api/v2/objects/7d5f985a-a0c4-4b4b-8550-2be98160e777",
                "actie": "create",
                "aanmaakdatum": "2021-09-22T21:29:21.541153Z",
                "kenmerken": {
                    "objectType": "Objecttypen API: productaanvraag"
                }
            }
        """.trimIndent()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/notification?connectorId=26141e07-40e4-4a7e-9c78-f7a40db3b3e9")
                .header("Authorization", "some-key")
                .content(postBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

        verifyRequestSent(HttpMethod.GET, "/api/v2/objects/7d5f985a-a0c4-4b4b-8550-2be98160e777")
        verifyRequestSent(HttpMethod.DELETE, "/api/v2/objects/7d5f985a-a0c4-4b4b-8550-2be98160e777")

        verify(burgerService).ensureBurgerExists("051845623")
    }

    @Test
    fun `should be able to use zaak during process when creating dossier with productaanvraag`() {
        prepareConnectorInstance("test-service-task")
        prepareOpenZaakConfig()
        prepareServiceTaskDocumentDefinitionSettings()

        `when`(burgerService.ensureBurgerExists("051845623")).thenReturn(Klant(
            "http://www.example.com/some-id",
            "0123456789",
            "test@example.com"
        ))

        val postBody = """
            {
                "kanaal": "objecten",
                "hoofdObject": "http://localhost:8000/api/v2/objects/7d5f985a-a0c4-4b4b-8550-2be98160e777",
                "resource": "object",
                "resourceUrl": "http://localhost:8000/api/v2/objects/7d5f985a-a0c4-4b4b-8550-2be98160e777",
                "actie": "create",
                "aanmaakdatum": "2021-09-22T21:29:21.541153Z",
                "kenmerken": {
                    "objectType": "Objecttypen API: productaanvraag"
                }
            }
        """.trimIndent()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/notification?connectorId=26141e07-40e4-4a7e-9c78-f7a40db3b3e9")
                .header("Authorization", "some-key")
                .content(postBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)

        verifyRequestSent(HttpMethod.GET, "/api/v2/objects/7d5f985a-a0c4-4b4b-8550-2be98160e777")
        verifyRequestSent(HttpMethod.DELETE, "/api/v2/objects/7d5f985a-a0c4-4b4b-8550-2be98160e777")

        val statusCreationRequest = findRequest(HttpMethod.POST, "/zaken/api/v1/statussen")
        assertNotNull(statusCreationRequest)
        val bodyContent = statusCreationRequest.body.readUtf8()
        assertEquals("${baseUrl}zaken/api/v1/zaken/7413e298-c78b-4ab8-8e8a-a825faed0e7f", JsonPath.read(bodyContent, "$.zaak"))
        assertEquals("http://example.com/catalogi/api/v1/statustypen/f8c938c1-e2ea-4cad-8025-f68248ad26ac", JsonPath.read(bodyContent, "$.statustype"))
    }

    fun setupMockServer() {
        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                executedRequests.add(request)
                val path = request.path?.substringBefore('?')
                val response = when (request.method) {
                    "GET" -> when (path) {
                        "/api/v2/objects/7d5f985a-a0c4-4b4b-8550-2be98160e777" -> handleProductAanvraagRequest()
                        "/api/v1/kanaal" -> handleKanaalListRequest()
                        "/documenten/api/v1/enkelvoudiginformatieobjecten/8b1568b4-cfbe-4151-bab0-787cd238814e" -> handleDocumentRequest(extractId(path))
                        "/documenten/api/v1/enkelvoudiginformatieobjecten/30ccb521-bf2c-452a-a24c-ed389a66b9fc" -> handleDocumentRequest(extractId(path))
                        "/catalogi/api/v1/zaaktypen" -> sendEmptyBodyResponse()
                        else -> MockResponse().setResponseCode(404)
                    }
                    "POST" -> when (path) {
                        "/api/v1/kanaal" -> handleKanaalCreateRequest()
                        "/api/v1/abonnement" -> handleAbonnementCreateRequest()
                        "/zaken/api/v1/zaken" -> handleZaakCreateRequest()
                        "/zaken/api/v1/rollen" -> handleZaakRolCreateRequest()
                        "/zaken/api/v1/zaakinformatieobjecten" -> handleZaakInformatieObjectCreateRequest(request)
                        "/zaken/api/v1/statussen" -> handleStatusCreateRequest()
                        else -> MockResponse().setResponseCode(404)
                    }
                    "DELETE" -> when (path) {
                        "/api/v1/abonnement/693fb25a-20ee-407b-917b-9dd0e76988c9" -> handleGenericDelete()
                        "/api/v2/objects/7d5f985a-a0c4-4b4b-8550-2be98160e777" -> handleGenericDelete()
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

    fun handleProductAanvraagRequest(): MockResponse {
        val body = """
            {
                "url": "http://localhost:8000/api/v2/objects/7d5f985a-a0c4-4b4b-8550-2be98160e777",
                "uuid": "7d5f985a-a0c4-4b4b-8550-2be98160e777",
                "type": "http://localhost:8000/api/v1/objecttypes/021f685e-9482-4620-b157-34cd4003da6b",
                "record": {
                    "index": 1,
                    "typeVersion": 1,
                    "data": {
                        "bsn": "051845623",
                        "data": {
                            "voornaam": "Henk"
                        },
                        "type": "some-type",
                        "pdf_url": "http://localhost:8000/documenten/api/v1/enkelvoudiginformatieobjecten/8b1568b4-cfbe-4151-bab0-787cd238814e",
                        "attachments": [
                            "http://localhost:8000/documenten/api/v1/enkelvoudiginformatieobjecten/30ccb521-bf2c-452a-a24c-ed389a66b9fc"
                        ],
                        "submission_id": "123"
                    },
                    "geometry": null,
                    "startAt": "2019-08-24",
                    "endAt": null,
                    "registrationAt": "2021-10-13",
                    "correctionFor": null,
                    "correctedBy": null
                }
            }
        """.trimIndent()

        return mockResponse(body)
    }

    fun handleDocumentRequest(documentId: UUID): MockResponse {
        val body = """
            {
                "url": "http://localhost:8000/documenten/api/v1/enkelvoudiginformatieobjecten/$documentId",
                "identificatie": "string",
                "bronorganisatie": "string",
                "creatiedatum": "2021-10-14",
                "titel": "string",
                "vertrouwelijkheidaanduiding": "openbaar",
                "auteur": "string",
                "status": "in_bewerking",
                "formaat": "string",
                "taal": "str",
                "versie": 0,
                "beginRegistratie": "2021-10-14T12:45:02Z",
                "bestandsnaam": "file.txt",
                "inhoud": "http://example.com",
                "bestandsomvang": 0,
                "link": "http://example.com",
                "beschrijving": "string",
                "ontvangstdatum": "2021-10-14",
                "verzenddatum": "2021-10-14",
                "indicatieGebruiksrecht": true,
                "ondertekening": {
                    "soort": "analoog",
                    "datum": "2021-10-14"
                },
                "integriteit": {
                    "algoritme": "crc_16",
                    "waarde": "string",
                    "datum": "2021-10-14"
                },
                "informatieobjecttype": "http://example.com",
                "locked": true,
                "bestandsdelen": [
                    {
                        "url": "http://example.com",
                        "volgnummer": 0,
                        "omvang": 0,
                        "inhoud": "http://example.com",
                        "voltooid": true,
                        "lock": "string"
                    }
                ]
            }
        """.trimIndent()

        return mockResponse(body)
    }

    fun handleZaakCreateRequest(): MockResponse {
        val body = """
            {
                "url":"${baseUrl}zaken/api/v1/zaken/7413e298-c78b-4ab8-8e8a-a825faed0e7f",
                "uuid":"7413e298-c78b-4ab8-8e8a-a825faed0e7f",
                "identificatie":"string",
                "bronorganisatie":"string",
                "omschrijving":"string",
                "toelichting":"string",
                "zaaktype":"${baseUrl}catalogi/api/v1/zaaktypen/4e9c2359-83ac-4e3b-96b6-3f278f1fc773",
                "registratiedatum":"2019-08-24",
                "verantwoordelijkeOrganisatie":"string",
                "startdatum":"2019-08-24",
                "einddatum":"2019-08-24",
                "einddatumGepland":"2019-08-24",
                "uiterlijkeEinddatumAfdoening":"2019-08-24",
                "publicatiedatum":"2019-08-24",
                "communicatiekanaal":"http://example.com",
                "productenOfDiensten":[
                    "http://example.com"
                ],
                "vertrouwelijkheidaanduiding":"openbaar",
                "betalingsindicatie":"nvt",
                "betalingsindicatieWeergave":"string",
                "laatsteBetaaldatum":"2019-08-24T14:15:22Z",
                "zaakgeometrie":{
                    "type":"Point",
                    "coordinates":[
                        0,
                        0
                    ]
                },
                "verlenging":{
                    "reden":"string",
                    "duur":"string"
                },
                "opschorting":{
                    "indicatie":true,
                    "reden":"string"
                },
                "selectielijstklasse":"http://example.com",
                "hoofdzaak":null,
                "deelzaken":[

                ],
                "relevanteAndereZaken":[
                    {
                        "url":"http://example.com",
                        "aardRelatie":"vervolg"
                    }
                ],
                "eigenschappen":[
                    "http://example.com"
                ],
                "status":"http://example.com",
                "kenmerken":[
                    {
                        "kenmerk":"string",
                        "bron":"string"
                    }
                ],
                "archiefnominatie":"blijvend_bewaren",
                "archiefstatus":"nog_te_archiveren",
                "archiefactiedatum":"2019-08-24",
                "resultaat":"http://example.com"
            }
        """.trimIndent()

        return mockResponse(body)
    }

    fun handleZaakRolCreateRequest(): MockResponse {
        val body = """
            {
                "zaak":"${baseUrl}zaken/api/v1/zaken/7413e298-c78b-4ab8-8e8a-a825faed0e7f",
                "betrokkene": null,
                "betrokkeneType":"natuurlijk_persoon",
                "roltype":"${baseUrl}catalogi/api/v1/roltypen/020b1fa2-dc67-4463-a778-367787f7e486",
                "roltoelichting":"string",
                "indicatieMachtiging":"gemachtigde",
                "betrokkeneIdentificatie":{
                    "inpBsn":"string",
                    "anpIdentificatie":"string",
                    "inpA_nummer":"string",
                    "geslachtsnaam":"string",
                    "voorvoegselGeslachtsnaam":"string",
                    "voorletters":"string",
                    "voornamen":"string",
                    "geslachtsaanduiding":"m",
                    "geboortedatum":"string",
                    "verblijfsadres":{
                        "aoaIdentificatie":"string",
                        "wplWoonplaatsNaam":"string",
                        "gorOpenbareRuimteNaam":"string",
                        "aoaPostcode":"string",
                        "aoaHuisnummer":0,
                        "aoaHuisletter":"s",
                        "aoaHuisnummertoevoeging":"stri",
                        "inpLocatiebeschrijving":"string"
                    },
                    "subVerblijfBuitenland":{
                        "lndLandcode":"stri",
                        "lndLandnaam":"string",
                        "subAdresBuitenland_1":"string",
                        "subAdresBuitenland_2":"string",
                        "subAdresBuitenland_3":"string"
                    }
                }
            }
        """.trimIndent()

        return mockResponse(body)
    }

    fun handleZaakInformatieObjectCreateRequest(request: RecordedRequest): MockResponse {
        val requestBody = request.body.readUtf8()
        val informatieobject: String = JsonPath.read(requestBody, "$.informatieobject")
        val zaak: String = JsonPath.read(requestBody, "$.zaak")
        val uuid = UUID.randomUUID()

        val body = """
            {
                "url":"${baseUrl}zaken/api/v1/zaakinformatieobjecten/${uuid}",
                "uuid":"${uuid}",
                "informatieobject":"${informatieobject}",
                "zaak":"${zaak}",
                "aardRelatieWeergave":"Hoort bij, omgekeerd: kent",
                "titel":"string",
                "beschrijving":"string",
                "registratiedatum":"2019-08-24T14:15:22Z"
            }
        """.trimIndent()

        return mockResponse(body)
    }

    fun handleKanaalCreateRequest(): MockResponse {
        val body = """
            {
                "url": "http://localhost:8000/api/v1/kanaal/75de0cc1-7695-45bb-a355-8bcde6e66db6",
                "naam": "objecten",
                "documentatieLink": "http://example.com",
                "filters": []
            }
        """.trimIndent()

        return mockResponse(body)
    }

    fun handleKanaalListRequest(): MockResponse {
        val body = """
            []
        """.trimIndent()

        return mockResponse(body)
    }

    fun handleAbonnementCreateRequest(): MockResponse {
        val body = """
            {
                "url": "http://localhost:8000/api/v1/abonnement/fd876e8c-f081-4093-9157-7b21645043dd",
                "callbackUrl": "http://example.com",
                "auth": "string",
                "kanalen": [
                    {
                        "filters": {},
                        "naam": "objecten"
                    }
                ]
            }
        """.trimIndent()

        return mockResponse(body)
    }

    fun handleStatusCreateRequest(): MockResponse {
        val body = """
            {
                "url": "http://example.com",
                "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
                "zaak": "http://example.com",
                "statustype": "http://example.com",
                "datumStatusGezet": "2019-08-24T14:15:22Z",
                "statustoelichting": "string"
            }
        """.trimIndent()

        return mockResponse(body)
    }

    fun handleGenericDelete(): MockResponse {
        return MockResponse()
            .setResponseCode(204)
    }

    fun sendEmptyBodyResponse(): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(200)
            .setBody("{}")
    }

    fun prepareConnectorInstance(processDefinitionKey: String = "test") {
        val connectorInstanceId = ConnectorInstanceId.newId(UUID.fromString("26141e07-40e4-4a7e-9c78-f7a40db3b3e9"))
        productAanvraagConnectorInstance = ConnectorInstance(
            connectorInstanceId,
            connectorType,
            "test-connector",
            ProductAanvraagProperties(
                ObjectsApiProperties(
                    ServerAuthSpecification(
                        baseUrl,
                        "token"
                    ),
                    ServerAuthSpecification(
                        baseUrl,
                        "token"
                    ),
                    ObjectTypeConfig(
                        "productaanvraag",
                        "Productaanvraag",
                        "${baseUrl}api/v1/objecttypes/021f685e-9482-4620-b157-34cd4003da6b",
                        "1"
                    )
                ),
                OpenNotificatieProperties(
                    baseUrl,
                    "clientId",
                    "69b8c79e-acb3-4587-9a2e-b9d288081c22",
                    baseUrl
                ),
                listOf(
                    ProductAanvraagTypeMapping(
                        "some-type",
                        "testschema",
                        processDefinitionKey
                    )
                ),
                "${baseUrl}catalogi/api/v1/roltypen/1c359a1b-c38d-47b8-bed5-994db88ead61"
            )
        )
        connectorTypeInstanceRepository.save(productAanvraagConnectorInstance)

        abonnementLink = AbonnementLink(
            connectorInstanceId,
            UUID.fromString("693fb25a-20ee-407b-917b-9dd0e76988c9"),
            "some-key"
        )
        abonnementLinkRepository.save(abonnementLink)

    }

    fun prepareOpenZaakConfig() {
        val connectorInstanceId = ConnectorInstanceId.newId(UUID.fromString("26141e07-40e4-4a7e-9c78-f7a40db3b3f0"))
        val openZaakConnectorType = connectorService.getConnectorTypes()
            .filter { it.name.equals("OpenZaak") }
            .first()

        openZaakConnectorInstance = ConnectorInstance(
            connectorInstanceId,
            openZaakConnectorType,
            "OpenZaakConnector",
            OpenZaakProperties(
                OpenZaakConfig(
                    baseUrl,
                    "test-client",
                    "711de9a3-1af6-4196-b4dd-e8a2e2ade17c",
                    Rsin("051845623")
                )
            )
        )
        connectorTypeInstanceRepository.save(openZaakConnectorInstance)

        zaakTypeLinkId = zaakTypeLinkService.createZaakTypeLink(CreateZaakTypeLinkRequest(
            "testschema",
            URI("${baseUrl}catalogi/api/v1/zaaktypen/4e9c2359-83ac-4e3b-96b6-3f278f1fc773"),
            true
        )).zaakTypeLink()?.id!!
    }

    fun prepareDocumentDefinitionSettings() {
        processDocumentAssociationService.createProcessDocumentDefinition(
            ProcessDocumentDefinitionRequest(
                "test",
                "testschema",
                true
            )
        )
    }

    fun prepareServiceTaskDocumentDefinitionSettings() {
        processDocumentAssociationService.createProcessDocumentDefinition(
            ProcessDocumentDefinitionRequest(
                "test-service-task",
                "testschema",
                true
            )
        )
        zaakTypeLinkService.assignServiceTaskHandler(
            zaakTypeLinkId,
            ServiceTaskHandlerRequest(
                "test-service-task",
                "change-status",
                Operation.SET_STATUS,
                URI("http://example.com/catalogi/api/v1/statustypen/f8c938c1-e2ea-4cad-8025-f68248ad26ac")
            )
        )
    }

    fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(200)
            .setBody(body)
    }

    fun extractId(url: String): UUID {
        return UUID.fromString(url.substringAfterLast("/"))
    }
}