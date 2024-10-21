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

package com.ritense.zakenapi.resolver

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.form.repository.FormDefinitionRepository
import com.ritense.form.service.PrefillFormService
import com.ritense.zakenapi.BaseIntegrationTest
import com.ritense.zakenapi.ZakenApiAuthentication
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClient
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono

@Transactional
class ZaakValueResolverValueIT @Autowired constructor(
    private val documentService: JsonSchemaDocumentService,
    private val formDefinitionRepository: FormDefinitionRepository,
    private val prefillFormService: PrefillFormService,
) : BaseIntegrationTest() {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    lateinit var server: MockWebServer

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockZakenApiServer()
        server.start(port = 56273)
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should prefill form with data from the Zaken API`() {
        runWithoutAuthorization {
            val documentId = documentService.createDocument(
                NewDocumentRequest("profile", objectMapper.createObjectNode())
            ).resultingDocument().get().id.id

            val formDefinition = formDefinitionRepository.findByName("form-with-zaak-fields").get()
            val prefilledFormDefinition = prefillFormService.getPrefilledFormDefinition(
                formDefinition.id!!,
                documentId
            )
            assertThat(
                JsonPath.read<List<String>>(
                    prefilledFormDefinition.asJson().toString(),
                    "$.components[?(@.properties.sourceKey=='zaak:identificatie')].defaultValue"
                ).toString()
            ).isEqualTo("""["ZK2023-00001"]""")
        }
    }

    private fun setupMockZakenApiServer() {
        server.dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                val response = when (request.requestLine) {
                    "GET /zaken/57f66ff6-db7f-43bc-84ef-6847640d3609 HTTP/1.1" -> getZaakRequest()
                    else -> MockResponse().setResponseCode(404)
                }
                return response
            }
        }
    }

    private fun getZaakRequest(): MockResponse {
        val body = """
            {
                "url": "http://localhost:56273/zaken/a6b63eb5-cc92-4f4b-ba53-9c145133166b",
                "uuid": "a6b63eb5-cc92-4f4b-ba53-9c145133166b",
                "identificatie": "ZK2023-00001",
                "bronorganisatie": "104978119",
                "omschrijving": "Test",
                "toelichting": "",
                "zaaktype": "http://localhost:56273/catalogi/e02753ba-9055-11ee-b9d1-0242ac120002",
                "registratiedatum": "2023-03-22",
                "verantwoordelijkeOrganisatie": "104978119",
                "startdatum": "2023-03-22",
                "einddatum": null,
                "einddatumGepland": "2023-05-17",
                "uiterlijkeEinddatumAfdoening": null,
                "publicatiedatum": null,
                "communicatiekanaal": "",
                "productenOfDiensten": [],
                "vertrouwelijkheidaanduiding": "openbaar",
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
                "rollen": [],
                "status": null,
                "zaakinformatieobjecten": [],
                "zaakobjecten": [],
                "kenmerken": [],
                "archiefnominatie": "blijvend_bewaren",
                "archiefstatus": "nog_te_archiveren",
                "archiefactiedatum": null,
                "resultaat": null,
                "opdrachtgevendeOrganisatie": "",
                "processobjectaard": "",
                "resultaattoelichting": "",
                "startdatumBewaartermijn": null
            },
        """.trimIndent()
        return mockResponse(body)
    }

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

    class TestAuthentication : ZakenApiAuthentication {
        override fun applyAuth(builder: RestClient.Builder): RestClient.Builder {
            return builder
        }

        override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
            return next.exchange(request)
        }
    }

    companion object {
        private const val PROCESS_DEFINITION_KEY = "zaken-api-plugin"
        private const val DOCUMENT_DEFINITION_KEY = "profile"
        private const val INFORMATIE_OBJECT_URL = "http://informatie.object.url"
    }

}
