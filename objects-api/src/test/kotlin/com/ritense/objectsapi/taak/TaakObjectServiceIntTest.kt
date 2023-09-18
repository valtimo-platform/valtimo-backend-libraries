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

package com.ritense.objectsapi.taak

import com.jayway.jsonpath.JsonPath
import com.ritense.connector.domain.Connector
import com.ritense.connector.repository.ConnectorTypeInstanceRepository
import com.ritense.document.domain.impl.Mapper
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.objectsapi.BaseIntegrationTest
import com.ritense.openzaak.domain.configuration.Rsin
import com.ritense.openzaak.domain.connector.OpenZaakConfig
import com.ritense.openzaak.domain.connector.OpenZaakProperties
import com.ritense.openzaak.domain.request.CreateZaakTypeLinkRequest
import com.ritense.openzaak.service.ZaakTypeLinkService
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentService
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod

internal class TaakObjectServiceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var taakObjectConnector: TaakObjectConnector

    @Autowired
    lateinit var processDocumentAssociationService: ProcessDocumentAssociationService

    @Autowired
    lateinit var processDocumentService: ProcessDocumentService

    @Autowired
    lateinit var taakObjectConnectorProperties: TaakProperties

    @Autowired
    lateinit var zaakTypeLinkService: ZaakTypeLinkService

    @Autowired
    lateinit var connectorTypeInstanceRepository: ConnectorTypeInstanceRepository

    @Autowired
    @Qualifier("openZaakConnector")
    lateinit var openZaakConnector: Connector

    lateinit var server: MockWebServer
    lateinit var executedRequests: MutableList<RecordedRequest>

    private val PROCESS_DEFINITION_KEY = "portal-task"
    private val DOCUMENT_DEFINITION_KEY = "testschema"

    @BeforeEach
    internal fun setUp() {
        startMockServer()
        setupTaakConnector()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should create task object`() {
        // given
        processDocumentAssociationService.createProcessDocumentDefinition(
            ProcessDocumentDefinitionRequest(PROCESS_DEFINITION_KEY, DOCUMENT_DEFINITION_KEY, true, true)
        )
        setupOpenZaakConnector()
        zaakTypeLinkService.createZaakTypeLink(
            CreateZaakTypeLinkRequest(
                DOCUMENT_DEFINITION_KEY,
                URI("http://some-url/catalogi/api/v1/zaaktypen/4e9c2359-83ac-4e3b-96b6-3f278f1fc773"),
                true
            )
        )
        val jsonContent = Mapper.INSTANCE.get().readTree("{\"voornaam\": \"Peter\"}")
        val newDocumentRequest = NewDocumentRequest(DOCUMENT_DEFINITION_KEY, jsonContent)
        val request = NewDocumentAndStartProcessRequest(PROCESS_DEFINITION_KEY, newDocumentRequest)
            .withProcessVars(mapOf("age" to 38))

        // when
        processDocumentService.newDocumentAndStartProcess(request)

        // then
        val createRequest = findRequest(HttpMethod.POST, "/api/v2/objects")
        assertNotNull(createRequest)
        val bodyContent = createRequest.body.readUtf8()
        assertEquals("test-form", JsonPath.read(bodyContent, "$.record.data.formulier_id"))
        assertEquals("12345", JsonPath.read(bodyContent, "$.record.data.bsn"))
        assertNotNull(JsonPath.read(bodyContent, "$.record.data.verwerker_taak_id"))
        assertEquals("Peter", JsonPath.read(bodyContent, "$.record.data.data.voornaam"))
        assertEquals(38, JsonPath.read(bodyContent, "$.record.data.data.leeftijd"))
        assertEquals("open", JsonPath.read(bodyContent, "$.record.data.status"))
    }

    private fun setupTaakConnector() {
        setupObjectApiConnector(server.url("/").toString())
        setupOpenNotificatieConnector(server.url("/").toString())
        taakObjectConnectorProperties.openNotificatieConnectionName = "openNotificatieInstance"
        taakObjectConnectorProperties.objectsApiConnectionName = "objectsApiInstance"

        connectorDeploymentService.deployAll(listOf(taakObjectConnector))
        val connectorType = connectorService.getConnectorTypes().first { it.name == "Taak" }

        connectorService.createConnectorInstance(
            connectorType.id.id,
            "TaakConnector",
            taakObjectConnectorProperties
        )

        taakObjectConnector = connectorService.loadByClassName(taakObjectConnector::class.java)
    }

    fun startMockServer() {
        executedRequests = mutableListOf()
        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                executedRequests.add(request)
                val response = when (request.method + " " + request.path?.substringBefore('?')) {
                    "POST /api/v2/objects" -> mockResponseFromFile("/data/post-create-object.json")
                    "POST /zaken/api/v1/zaken" -> mockResponseFromFile("/data/post-create-zaak.json")
                    "GET /zaken/api/v1/rollen" -> mockResponseFromFile("/data/get-rol.json")
                    "GET /api/v1/kanaal" -> mockResponseFromFile("/data/get-kanalen.json")
                    "POST /api/v1/abonnement" -> mockResponseFromFile("/data/post-abonnement.json")
                    else -> MockResponse().setResponseCode(404)
                }
                return response
            }
        }
        server = MockWebServer()
        server.dispatcher = dispatcher
        server.start()
    }

    private fun setupOpenZaakConnector() {
        val properties = OpenZaakProperties(
            OpenZaakConfig(
                server.url("/").toString(),
                "test-client",
                "711de9a3-1af6-4196-b4dd-e8a2e2ade17c",
                Rsin("051845623")
            )
        )
        connectorDeploymentService.deployAll(listOf(openZaakConnector))
        val connectorType = connectorService.getConnectorTypes().first { it.name == "OpenZaak" }

        connectorService.createConnectorInstance(
            connectorType.id.id,
            "openZaakInstance",
            properties
        )

        openZaakConnector = connectorService.loadByClassName(openZaakConnector::class.java)
    }

    fun findRequest(method: HttpMethod, path: String): RecordedRequest? {
        return executedRequests
            .filter { method.matches(it.method!!) }
            .firstOrNull { it.path?.substringBefore('?').equals(path) }
    }

    private fun mockResponseFromFile(fileName: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(200)
            .setBody(this::class.java.getResource(fileName).readText(Charsets.UTF_8))
    }

}