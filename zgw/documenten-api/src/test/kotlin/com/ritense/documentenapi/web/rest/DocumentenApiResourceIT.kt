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

package com.ritense.documentenapi.web.rest

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.documentenapi.BaseIntegrationTest
import com.ritense.documentenapi.DocumentenApiAuthentication
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.valtimo.contract.json.Mapper
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doCallRealMethod
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import java.util.Optional
import java.util.UUID
import javax.transaction.Transactional

@Transactional
internal class DocumentenApiResourceIT : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    lateinit var mockMvc: MockMvc

    lateinit var server: MockWebServer

    lateinit var pluginConfiguration: PluginConfiguration

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()

        server = MockWebServer()
        setupMockDocumentenApiServer()
        server.start()

        val mockedId = PluginConfigurationId.existingId(UUID.fromString("c850401b-9331-4cb6-8f1c-3e34b12e3d55"))
        doReturn(Optional.of(mock<PluginConfiguration>())).whenever(pluginConfigurationRepository).findById(mockedId)
        doReturn(TestAuthentication()).whenever(pluginService).createInstance(mockedId)
        doCallRealMethod().whenever(pluginService).createPluginConfiguration(any(), any(), any())

        pluginConfiguration = pluginService.createPluginConfiguration(
            "Documenten API plugin configuration",
            Mapper.INSTANCE.get().readTree(
                """
                    {
                        "url": "${server.url("/")}",
                        "bronorganisatie": "123456789",
                        "authenticationPluginConfiguration": "c850401b-9331-4cb6-8f1c-3e34b12e3d55"
                    }
                """.trimIndent()
            ) as ObjectNode,
            "documentenapi"
        )
    }

    @Test
    @WithMockUser(USER_EMAIL)
    fun `should download document from documeten API`() {
        mockMvc.perform(
            get(
                "/api/v1/documenten-api/{documentenapiConfigurationId}/files/{documentId}/download",
                pluginConfiguration.id.id,
                DOCUMENT_ID
            ).contentType(MediaType.APPLICATION_OCTET_STREAM)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"passport.jpg\""))
            .andExpect(header().string("Content-Type", "image/jpeg"))
            .andExpect(content().string("TEST_DOCUMENT_CONTENT"))
    }


    private fun setupMockDocumentenApiServer() {
        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                val response = when (request.path?.substringBefore('?')) {
                    "/enkelvoudiginformatieobjecten/$DOCUMENT_ID" -> handleDocumentRequest()
                    "/enkelvoudiginformatieobjecten/$DOCUMENT_ID/download" -> handleDocumentDownloadRequest()
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
              "bronorganisatie": "404797441",
              "creatiedatum": "2019-08-24",
              "titel": "Passport",
              "vertrouwelijkheidaanduiding": "openbaar",
              "auteur": "string",
              "status": "in_bewerking",
              "formaat": "string",
              "taal": "str",
              "versie": 0,
              "beginRegistratie": "2019-08-24T14:15:22Z",
              "bestandsnaam": "passport.jpg",
              "inhoud": "string",
              "bestandsomvang": 0,
              "link": "http://example.com",
              "beschrijving": "My passport",
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

    private fun handleDocumentDownloadRequest(): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/octet-stream")
            .setBody("TEST_DOCUMENT_CONTENT")
    }

    class TestAuthentication : DocumentenApiAuthentication {
        override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
            return next.exchange(request)
        }
    }

    companion object {
        private const val USER_EMAIL = "user@valtimo.nl"
        private const val DOCUMENT_ID = "3bd88200-11cb-45cf-a742-da01261755b1"
    }
}
