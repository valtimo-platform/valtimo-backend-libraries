package com.ritense.zakenapi

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.plugin.domain.ActivityType
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.plugin.domain.PluginProcessLinkId
import com.ritense.plugin.repository.PluginProcessLinkRepository
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processdocument.service.impl.result.NewDocumentAndStartProcessResultSucceeded
import com.ritense.valtimo.contract.json.Mapper
import com.ritense.valtimo.contract.resource.Resource
import java.net.URI
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.camunda.bpm.engine.RepositoryService
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
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID
import javax.transaction.Transactional
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

    lateinit var server: MockWebServer

    lateinit var processDefinitionId: String

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockZakenApiServer()
        server.start()

        // Since we do not have an actual authentication plugin in this context we will mock one
        val mockedId = PluginConfigurationId.existingId(UUID.fromString("27a399c7-9d70-4833-a651-57664e2e9e09"))
        doReturn(Optional.of(mock<PluginConfiguration>())).whenever(pluginConfigurationRepository).findById(mockedId)
        doReturn(TestAuthentication()).whenever(pluginService).createInstance(mockedId)
        doCallRealMethod().whenever(pluginService).createPluginConfiguration(any(), any(), any())

        whenever(zaakUrlProvider.getZaakUrl(any())).thenReturn(ZAAK_URL)

        // Setting up plugin
        val pluginPropertiesJson = """
            {
              "url": "${server.url("/")}",
              "authenticationPluginConfiguration": "27a399c7-9d70-4833-a651-57664e2e9e09"
            }
        """.trimIndent()

        val configuration = pluginService.createPluginConfiguration(
            "Zaken API plugin configuration",
            Mapper.INSTANCE.get().readTree(
                pluginPropertiesJson
            ) as ObjectNode,
            "zakenapi"
        )

        val actionPropertiesJson = """
            {
                "documentUrl" : "${INFORMATIE_OBJECT_URL}",
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
                Mapper.INSTANCE.get().readTree(actionPropertiesJson) as ObjectNode,
                configuration.id,
                "link-document-to-zaak",
                ActivityType.SERVICE_TASK_START
            )
        )
    }

    @Test
    fun `should link document to zaak`() {
        val newDocumentRequest = NewDocumentRequest(DOCUMENT_DEFINITION_KEY, Mapper.INSTANCE.get().createObjectNode())
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
        val response = procesDocumentService.newDocumentAndStartProcess(request)
        assertTrue(response is NewDocumentAndStartProcessResultSucceeded)

        // Check the request that was sent to the open zaak api
        val recordedRequest = server.takeRequest()
        val requestString = recordedRequest.body.readUtf8()
        val parsedOutput = Mapper.INSTANCE.get().readValue(requestString, Map::class.java)

        assertEquals(4, parsedOutput.size)
        assertEquals(INFORMATIE_OBJECT_URL, parsedOutput["informatieobject"])
        assertEquals(ZAAK_URL.toString(), parsedOutput["zaak"])
        assertEquals("titelVariableName", parsedOutput["titel"])
        assertEquals("beschrijvingVariableName", parsedOutput["beschrijving"])

        // Check to see if the document is correctly linked inside the valtimo database as well
        assertNotNull(response.resultingDocument())
        assertTrue(response.resultingDocument().isPresent)
        val processDocumentId = response.resultingDocument().get().id().id
        assertNotNull(documentService.get(processDocumentId.toString()))
    }

    @Test
    fun `should link uploaded document to zaak`() {
        val newDocumentRequest = NewDocumentRequest(DOCUMENT_DEFINITION_KEY, Mapper.INSTANCE.get().createObjectNode())
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
        val response = procesDocumentService.newDocumentAndStartProcess(request)
        assertTrue(response is NewDocumentAndStartProcessResultSucceeded)

        // Check the request that was sent to the open zaak api
        val recordedRequest = server.takeRequest()
        val requestString = recordedRequest.body.readUtf8()
        val parsedOutput = Mapper.INSTANCE.get().readValue(requestString, Map::class.java)

        assertEquals(4, parsedOutput.size)
        assertEquals(INFORMATIE_OBJECT_URL, parsedOutput["informatieobject"])
        assertEquals(ZAAK_URL.toString(), parsedOutput["zaak"])
        assertEquals("titelVariableName", parsedOutput["titel"])
        assertEquals("beschrijvingVariableName", parsedOutput["beschrijving"])

        // Check to see if the document is correctly linked inside the valtimo database as well
        assertNotNull(response.resultingDocument())
        assertTrue(response.resultingDocument().isPresent)
        val processDocumentId = response.resultingDocument().get().id().id
        assertNotNull(documentService.get(processDocumentId.toString()))
    }

    private fun setupMockZakenApiServer() {
        val dispatcher: Dispatcher = object: Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path?.substringBefore('?')
                val response = when(path) {
                    "/zaakinformatieobjecten" -> handleZaakInformatieObjectRequest()
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
              "informatieobject": "${INFORMATIE_OBJECT_URL}",
              "zaak": "http://example.com",
              "aardRelatieWeergave": "Hoort bij, omgekeerd: kent",
              "titel": "string",
              "beschrijving": "string",
              "registratiedatum": "2019-08-24T14:15:22Z"
            }
        """.trimIndent()
        return mockResponse(body)
    }

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

    class TestAuthentication: ZakenApiAuthentication {
        override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
            return next.exchange(request)
        }
    }

    companion object {
        private const val PROCESS_DEFINITION_KEY = "zaken-api-plugin"
        private const val DOCUMENT_DEFINITION_KEY = "profile"
        private const val INFORMATIE_OBJECT_URL = "http://informatie.object.url"
        private val ZAAK_URL = URI("http://zaak.url")
    }
}
