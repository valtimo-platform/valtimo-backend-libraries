package com.ritense.objectsapi.taak

import com.jayway.jsonpath.JsonPath
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.connector.service.ConnectorDeploymentService
import com.ritense.connector.service.ConnectorService
import com.ritense.document.domain.impl.Mapper
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.objectsapi.BaseIntegrationTest
import com.ritense.openzaak.service.impl.model.ResultWrapper
import com.ritense.openzaak.service.impl.model.zaak.BetrokkeneType
import com.ritense.openzaak.service.impl.model.zaak.Rol
import com.ritense.openzaak.service.impl.model.zaak.RolNatuurlijkPersoon
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentService
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

internal class TaakObjectServiceIntTest: BaseIntegrationTest() {

    @Autowired
    lateinit var taakObjectConnector: TaakObjectConnector

    @Autowired
    lateinit var processDocumentAssociationService: ProcessDocumentAssociationService

    @Autowired
    lateinit var processDocumentService: ProcessDocumentService

    @Autowired
    lateinit var taakObjectConnectorProperties: TaakProperties

    @Autowired
    lateinit var connectorService: ConnectorService

    @Autowired
    lateinit var connectorDeploymentService: ConnectorDeploymentService

    lateinit var server: MockWebServer
    lateinit var executedRequests: MutableList<RecordedRequest>

    private val PROCESS_DEFINITION_KEY = "portal-task"
    private val DOCUMENT_DEFINITION_KEY = "testschema"

    @BeforeEach
    internal fun setUp() {
        startMockServer()
        setupConnector()
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

        whenever(zaakRolService.getZaakInitator(any())).thenReturn(ResultWrapper(count = 1, results = listOf(
            Rol(
                URI("http://some-url"),
                URI("http://some-url"),
                BetrokkeneType.NATUURLIJK_PERSOON,
                URI("http://some-url"),
                "toelichting",
                RolNatuurlijkPersoon(
                    "12345"
                )
            )
        )))

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
        assertEquals("38", JsonPath.read(bodyContent, "$.record.data.data.leeftijd"))
        assertEquals("open", JsonPath.read(bodyContent, "$.record.data.status"))
    }

    private fun setupConnector() {
        taakObjectConnectorProperties.objectsApiProperties.objectsApi.url = server.url("/").toString()
        taakObjectConnectorProperties.objectsApiProperties.objectsApi.token = "some-token"

        val types = connectorDeploymentService.deployAll(listOf(taakObjectConnector))
        val connectorType = connectorService.getConnectorTypes().first { it.name == "Taak" }

        connectorService.createConnectorInstance(
            connectorType.id.id,
            "",
            taakObjectConnectorProperties
        )

        taakObjectConnector = connectorService.loadByClassName(taakObjectConnector::class.java)
    }

    fun startMockServer() {
        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                executedRequests.add(request)
                val response = when (request.path?.substringBefore('?')) {
                    "/api/v2/objects" -> when (request.method) {
                        "POST" -> mockResponseFromFile("/data/post-create-object.json")
                        else -> MockResponse().setResponseCode(404)
                    }
                    else -> MockResponse().setResponseCode(404)
                }
                return response
            }
        }
        server = MockWebServer()
        server.dispatcher = dispatcher
        server.start()
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

    private fun mockResponseFromFile(fileName: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(200)
            .setBody(this::class.java.getResource(fileName).readText(Charsets.UTF_8))
    }

}