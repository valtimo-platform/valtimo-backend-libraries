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

@Transactional
class ZaakStatusValueResolverValueIT @Autowired constructor(
    private val documentService: JsonSchemaDocumentService,
    private val formDefinitionRepository: FormDefinitionRepository,
    private val prefillFormService: PrefillFormService,
    private val objectMapper: ObjectMapper,
) : BaseIntegrationTest() {

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
    fun `should prefill form with status data from the Zaken API`() {
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
                    "$.components[?(@.properties.sourceKey=='zaakstatus:omschrijving')].defaultValue"
                ).toString()
            ).isEqualTo("""["Zaak gestart"]""")
        }
    }

    private fun setupMockZakenApiServer() {
        server.dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                val response = when (request.requestLine) {
                    "GET /zaken/57f66ff6-db7f-43bc-84ef-6847640d3609 HTTP/1.1" -> getZaakRequest()
                    "GET /zaken/statussen/f0ca7629-115d-4231-b684-7eaa130ac1af HTTP/1.1" -> getZaakStatusRequest()
                    "GET /catalogi/statustypen/40cb531f-fbde-46af-9693-90e78535ff9f HTTP/1.1" -> getStatusTypeRequest()
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
                "status": "http://localhost:56273/zaken/statussen/f0ca7629-115d-4231-b684-7eaa130ac1af",
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
            }
        """.trimIndent()
        return mockResponse(body)
    }

    private fun getZaakStatusRequest(): MockResponse {
        val body = """
        {
            "url": "http://localhost:56273/zaken/f7ef2339-fe9a-435e-a2d3-ae75ca4fb51a",
            "uuid": "f7ef2339-fe9a-435e-a2d3-ae75ca4fb51a",
            "zaak": "http://localhost:56273/zaken/a6b63eb5-cc92-4f4b-ba53-9c145133166b",
            "statustype": "http://localhost:56273/catalogi/statustypen/40cb531f-fbde-46af-9693-90e78535ff9f",
            "datumStatusGezet": "2023-12-01T14:52:07Z",
            "statustoelichting": "",
            "indicatieLaatstGezetteStatus": true
        }
        """.trimIndent()
        return mockResponse(body)
    }

    private fun getStatusTypeRequest(): MockResponse {
        val body = """
        {
            "url": "http://localhost:56273/catalogi/statustypen/33e13d2c-6441-4d70-a30b-bdda74105c1f",
            "omschrijving": "Zaak gestart",
            "omschrijvingGeneriek": "Zaak gestart",
            "statustekst": "",
            "zaaktype": "http://localhost:56273/catalogi/zaaktypen/01afac88-36e0-466b-b233-b8e2301c57e2",
            "zaaktypeIdentificatie": "TEST_AANV",
            "volgnummer": 1,
            "checklistitemStatustype": [],
            "catalogus": "http://localhost:56273/catalogi/759c3861-9c86-44f6-9c17-178ce9c331a7"
        }
        """.trimIndent()
        return mockResponse(body)
    }

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

}
