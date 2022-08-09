/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.documentenapi

import com.fasterxml.jackson.databind.node.ObjectNode
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doCallRealMethod
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.plugin.domain.PluginProcessLinkId
import com.ritense.plugin.repository.PluginProcessLinkRepository
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.resource.domain.MetadataType
import com.ritense.resource.repository.OpenZaakResourceRepository
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.valtimo.contract.json.Mapper
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDate
import java.util.Optional
import java.util.UUID
import javax.transaction.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Transactional
internal class DocumentenApiPluginIT: BaseIntegrationTest(){

    @Autowired
    lateinit var repositoryService: RepositoryService

    @Autowired
    lateinit var runtimeService: RuntimeService

    @Autowired
    lateinit var processDocumentService: ProcessDocumentService

    @Autowired
    lateinit var pluginProcessLinkRepository: PluginProcessLinkRepository

    @Autowired
    lateinit var temporaryResourceStorageService: TemporaryResourceStorageService

    @Autowired
    lateinit var openZaakResourceRepository: OpenZaakResourceRepository

    lateinit var server: MockWebServer

    lateinit var processDefinitionId: String

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockDocumentenApiServer()
        server.start()

        val pluginPropertiesJson = """
            {
              "url": "${server.url("/")}",
              "bronorganisatie": "123456789",
              "authenticationPluginConfiguration": "c850401b-9331-4cb6-8f1c-3e34b12e3d55"
            }
        """

        //since we do not have an actual authentication plugin in this context we will mock one
        val mockedId = PluginConfigurationId.existingId(UUID.fromString("c850401b-9331-4cb6-8f1c-3e34b12e3d55"))
        doReturn(Optional.of(mock<PluginConfiguration>())).whenever(pluginConfigurationRepository).findById(mockedId)

        doReturn(TestAuthentication()).whenever(pluginService).createInstance(mockedId)
        doCallRealMethod().whenever(pluginService).createPluginConfiguration(any(), any(), any())

        val configuration = pluginService.createPluginConfiguration(
            "Documenten API plugin configuration",
            Mapper.INSTANCE.get().readTree(
                pluginPropertiesJson
            ) as ObjectNode,
            "documentenapi"
        )
        val actionProperties = """
            {
                "localDocumentLocation": "localDocumentVariableName",
                "storedDocumentUrl": "storedDocumentVariableName",
                "informatieobjecttype": "testtype",
                "taal": "nld",
                "status": "in_bewerking"
            }
        """.trimIndent()

        processDefinitionId = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey("documenten-api-plugin")
            .latestVersion()
            .singleResult()
            .id

        pluginProcessLinkRepository.save(
            PluginProcessLink(
                PluginProcessLinkId(UUID.randomUUID()),
                processDefinitionId,
                "StoreDocument",
                Mapper.INSTANCE.get().readTree(actionProperties) as ObjectNode,
                configuration.id,
                "store-temp-document"
            )
        )
    }

    @Test
    fun `should store temp file in documenten api`() {
        val documentId = temporaryResourceStorageService.store(
            "test".byteInputStream(),
            mapOf(
                MetadataType.FILE_NAME.name to "test.ext"
            )
        )

        val newDocumentRequest = NewDocumentRequest(DOCUMENT_DEFINITION_KEY, Mapper.INSTANCE.get().createObjectNode())
        val request = NewDocumentAndStartProcessRequest(PROCESS_DEFINITION_KEY, newDocumentRequest)
            .withProcessVars(mapOf("localDocumentVariableName" to documentId))

        processDocumentService.newDocumentAndStartProcess(request)

        val resourceId = runtimeService.createVariableInstanceQuery()
            .variableName("storedDocumentVariableName")
            .singleResult()
            .value as String

        val resource = openZaakResourceRepository.findByInformatieObjectUrl(URI("http://example.com"))
        assertNotNull(resource)

        val recordedRequest = server.takeRequest()
        val requestString = recordedRequest.body.readUtf8()

        val parsedOutput = Mapper.INSTANCE.get().readValue(requestString, Map::class.java)

        assertEquals("123456789", parsedOutput["bronorganisatie"])
        assertEquals(LocalDate.now().toString(), parsedOutput["creatiedatum"])
        assertEquals("test.ext", parsedOutput["titel"])
        assertEquals("GZAC", parsedOutput["auteur"])
        assertEquals("test.ext", parsedOutput["bestandsnaam"])
        assertEquals("nld", parsedOutput["taal"])
        assertEquals("dGVzdA==", parsedOutput["inhoud"])
        assertEquals("testtype", parsedOutput["informatieobjecttype"])
        assertEquals("in_bewerking", parsedOutput["status"])
        assertEquals(false, parsedOutput["indicatieGebruiksrecht"])

        assertEquals("http://example.com", resourceId)
    }

    fun setupMockDocumentenApiServer() {
        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path?.substringBefore('?')
                val response = when (path) {
                    "/enkelvoudiginformatieobjecten"
                    -> handleDocumentRequest()
                    else -> MockResponse().setResponseCode(404)
                }
                return response
            }
        }
        server.dispatcher = dispatcher
    }

    private fun handleDocumentRequest(): MockResponse {
        val body = """
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
        return mockResponse(body)
    }

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

    class TestAuthentication: DocumentenApiAuthentication {
        override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
            return next.exchange(request)
        }
    }

    companion object {
        private const val PROCESS_DEFINITION_KEY = "documenten-api-plugin"
        private const val DOCUMENT_DEFINITION_KEY = "profile"
    }
}