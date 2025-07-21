/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.klantinteractiesapi.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import com.ritense.klantinteractiesapi.KlantinteractiesApiAuthentication
import com.ritense.klantinteractiesapi.client.dto.CreatePartijIdentificatorLink
import com.ritense.klantinteractiesapi.client.dto.CreatePartijRequest
import com.ritense.klantinteractiesapi.domain.CodeObjecttype
import com.ritense.klantinteractiesapi.domain.CodeRegister
import com.ritense.klantinteractiesapi.domain.Contactnaam
import com.ritense.klantinteractiesapi.domain.PartijIdentificator
import com.ritense.klantinteractiesapi.domain.PartijSoort
import com.ritense.klantinteractiesapi.domain.PersoonPartijIndentificatie
import com.ritense.klantinteractiesapi.domain.SoortObject
import com.ritense.valtimo.contract.json.MapperSingleton
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestClient
import java.net.URI

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KlantinteractiesApiClientTest {
    lateinit var mockApi: MockWebServer
    lateinit var objectMapper: ObjectMapper
    lateinit var restClientBuilder: RestClient.Builder

    @BeforeAll
    fun setUp() {
        objectMapper = MapperSingleton.get().copy().apply {
            this.registerSubtypes(NamedType(PersoonPartijIndentificatie::class.java))
        }

        restClientBuilder = RestClient.builder().messageConverters { converters ->
            converters.forEach { converter ->
                if (converter is MappingJackson2HttpMessageConverter) {
                    converter.setObjectMapper(objectMapper)
                }
            }
        }

        mockApi = MockWebServer()
        mockApi.start()
    }

    @AfterAll
    fun tearDown() {
        mockApi.shutdown()
    }

    @Test
    fun `should send create partij request and parse response`() {
        val client = KlantinteractiesApiClient(restClientBuilder)
        val responseBody = """
            {
                "uuid": "00000000-aca0-4274-b371-0bb154aa0a59",
                "url": "http://localhost:0000/klantinteracties/api/v1/partijen/00000000-aca0-4274-b371-0bb154aa0a59",
                "nummer": "0000000004",
                "partijIdentificatoren": [
                    {
                        "uuid": "00000000-87e0-43d6-bb5d-1f424b07c216",
                        "url": "http://localhost:8006/klantinteracties/api/v1/partij-identificatoren/00000000-87e0-43d6-bb5d-1f424b07c216",
                        "identificeerdePartij": {
                            "uuid": "00000000-aca0-4274-b371-0bb154aa0a59",
                            "url": "http://localhost:8006/klantinteracties/api/v1/partijen/00000000-aca0-4274-b371-0bb154aa0a59"
                        },
                        "anderePartijIdentificator": "",
                        "partijIdentificator": {
                            "codeObjecttype": "natuurlijk_persoon",
                            "codeSoortObjectId": "bsn",
                            "objectId": "239668200",
                            "codeRegister": "brp"
                        },
                        "subIdentificatorVan": null
                    }
                ],
                "soortPartij": "persoon",
                "indicatieActief": true,
                "partijIdentificatie": {
                    "contactnaam": {
                        "voorletters": "J",
                        "voornaam": "John",
                        "voorvoegselAchternaam": "",
                        "achternaam": "Doe"
                    },
                    "volledigeNaam": "John Doe"
                }
            }
        """.trimIndent()
        mockApi.enqueue(mockResponse(responseBody))

        val partij = client.createPartij(
            TestAuthentication(),
            URI(mockApi.url("/").toString()),
            CreatePartijRequest(
                partijIdentificatoren = listOf(
                    CreatePartijIdentificatorLink(
                        partijIdentificator = PartijIdentificator(
                            codeObjecttype = CodeObjecttype.NATUURLIJK_PERSOON,
                            codeSoortObjectId = SoortObject.BSN,
                            objectId = "239668200",
                            codeRegister = CodeRegister.BRP,
                        )
                    )
                ),
                soortPartij = PartijSoort.PERSOON,
                indicatieActief = true,
                partijIdentificatie = PersoonPartijIndentificatie(
                    Contactnaam(
                        voorletters = "J",
                        voornaam = "John",
                        achternaam = "Doe",
                    )
                )
            )
        )

        val recordedRequest = mockApi.takeRequest()
        val body = recordedRequest.body.readUtf8()

        //validate request
        assertThat(
            body,
            hasJsonPath("$.partijIdentificatoren[0].partijIdentificator.codeObjecttype", equalTo("natuurlijk_persoon"))
        )
        assertThat(
            body,
            hasJsonPath("$.partijIdentificatoren[0].partijIdentificator.codeSoortObjectId", equalTo("bsn"))
        )
        assertThat(body, hasJsonPath("$.partijIdentificatoren[0].partijIdentificator.objectId", equalTo("239668200")))
        assertThat(body, hasJsonPath("$.partijIdentificatoren[0].partijIdentificator.codeRegister", equalTo("brp")))
        assertThat(body, hasJsonPath("$.soortPartij", equalTo("persoon")))
        assertThat(body, hasJsonPath("$.indicatieActief", equalTo(true)))
        assertThat(body, hasJsonPath("$.partijIdentificatie.contactnaam.voorletters", equalTo("J")))
        assertThat(body, hasJsonPath("$.partijIdentificatie.contactnaam.voornaam", equalTo("John")))
        assertThat(body, hasJsonPath("$.partijIdentificatie.contactnaam.achternaam", equalTo("Doe")))

        //validate response
        assertEquals(
            URI("http://localhost:0000/klantinteracties/api/v1/partijen/00000000-aca0-4274-b371-0bb154aa0a59"),
            partij.url
        )
        assertEquals(
            CodeObjecttype.NATUURLIJK_PERSOON,
            partij.partijIdentificatoren[0].partijIdentificator!!.codeObjecttype
        )
        assertEquals(SoortObject.BSN, partij.partijIdentificatoren[0].partijIdentificator!!.codeSoortObjectId)
        assertEquals("239668200", partij.partijIdentificatoren[0].partijIdentificator!!.objectId)
        assertEquals(CodeRegister.BRP, partij.partijIdentificatoren[0].partijIdentificator!!.codeRegister)
        assertEquals(PartijSoort.PERSOON, partij.soortPartij)
        assertEquals(true, partij.indicatieActief)
        assertEquals("J", (partij.partijIdentificatie as PersoonPartijIndentificatie).contactnaam!!.voorletters)
        assertEquals("John", (partij.partijIdentificatie as PersoonPartijIndentificatie).contactnaam!!.voornaam)
        assertEquals("Doe", (partij.partijIdentificatie as PersoonPartijIndentificatie).contactnaam!!.achternaam)
    }

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

    class TestAuthentication : KlantinteractiesApiAuthentication {

        override fun applyAuth(builder: RestClient.Builder): RestClient.Builder {
            return builder.defaultHeaders { headers ->
                headers.setBearerAuth("test")
            }
        }
    }
}