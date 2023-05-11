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

package com.ritense.besluitenapi.client

import com.jayway.jsonpath.matchers.JsonPathMatchers
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import com.ritense.besluitenapi.BesluitenApiAuthentication
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BesluitenApiClientTest {
    lateinit var mockApi: MockWebServer

    @BeforeAll
    fun setUp() {
        mockApi = MockWebServer()
        mockApi.start()
    }

    @AfterAll
    fun tearDown() {
        mockApi.shutdown()
    }

    @Test
    fun `should send create besluit request and parse response`() {
        val webclientBuilder = WebClient.builder()
        val client = BesluitenApiClient(webclientBuilder)

        val responseBody = """
            {
                "url": "http://besluit.api/besluit",
                "identificatie": "identificatie",
                "verantwoordelijkeOrganisatie": "633182801",
                "besluittype": "http://catalogus.api/besluittype",
                "zaak": "http://zaken.api/zaak",
                "datum": "2019-02-20",
                "toelichting": "toelichting",
                "bestuursorgaan": "680572442",
                "ingangsdatum": "2019-02-21",
                "vervaldatum": "2019-02-22",
                "vervalreden": "tijdelijk",
                "vervalredenWeergave": "reden",
                "publicatiedatum": "2019-02-23",
                "verzenddatum": "2019-02-24",
                "uiterlijkeReactiedatum": "2019-02-25"
            }
        """.trimIndent()

        mockApi.enqueue(mockResponse(responseBody))

        val besluit = client.createBesluit(
            TestAuthentication(),
            URI(mockApi.url("/").toString()),
            CreateBesluitRequest(
                zaak = URI("http://zaken.api/zaak"),
                besluittype = URI("http://catalogus.api/besluittype"),
                verantwoordelijkeOrganisatie = "633182801",
                datum = LocalDate.of(2020, 2, 20),
                ingangsdatum = LocalDate.of(2020, 2, 21),
                toelichting = "toelichting",
                bestuursorgaan = "680572442",
                vervaldatum = LocalDate.of(2020, 2, 22),
                vervalreden = Vervalreden.TIJDELIJK,
                publicatiedatum = LocalDate.of(2020, 2, 23),
                verzenddatum = LocalDate.of(2020, 2, 24),
                uiterlijkeReactiedatum = LocalDate.of(2020, 2, 25)
            )
        )

        val recordedRequest = mockApi.takeRequest()
        val body = recordedRequest.body.readUtf8()

        //validate request
        assertThat(body, jsonPathMissingOrNull("$.identificatie"))
        assertThat(body, hasJsonPath("$.verantwoordelijkeOrganisatie", equalTo("633182801")))
        assertThat(body, hasJsonPath("$.besluittype", equalTo("http://catalogus.api/besluittype")))
        assertThat(body, hasJsonPath("$.zaak", equalTo("http://zaken.api/zaak")))
        assertThat(body, hasJsonPath("$.datum", equalTo("2020-02-20")))
        assertThat(body, hasJsonPath("$.toelichting", equalTo("toelichting")))
        assertThat(body, hasJsonPath("$.bestuursorgaan", equalTo("680572442")))
        assertThat(body, hasJsonPath("$.ingangsdatum", equalTo("2020-02-21")))
        assertThat(body, hasJsonPath("$.vervaldatum", equalTo("2020-02-22")))
        assertThat(body, hasJsonPath("$.vervalreden", equalTo("tijdelijk")))
        assertThat(body, hasJsonPath("$.publicatiedatum", equalTo("2020-02-23")))
        assertThat(body, hasJsonPath("$.verzenddatum", equalTo("2020-02-24")))
        assertThat(body, hasJsonPath("$.uiterlijkeReactiedatum", equalTo("2020-02-25")))

        //validate response
        assertEquals(URI("http://besluit.api/besluit"), besluit.url)
        assertEquals("identificatie", besluit.identificatie)
        assertEquals("633182801", besluit.verantwoordelijkeOrganisatie)
        assertEquals(URI("http://catalogus.api/besluittype"), besluit.besluittype)
        assertEquals(URI("http://zaken.api/zaak"), besluit.zaak)
        assertEquals(LocalDate.of(2019, 2, 20), besluit.datum)
        assertEquals("toelichting", besluit.toelichting)
        assertEquals("680572442", besluit.bestuursorgaan)
        assertEquals(LocalDate.of(2019, 2, 21), besluit.ingangsdatum)
        assertEquals(LocalDate.of(2019, 2, 22), besluit.vervaldatum)
        assertEquals(Vervalreden.TIJDELIJK, besluit.vervalreden)
        assertEquals("reden", besluit.vervalredenWeergave)
        assertEquals(LocalDate.of(2019, 2, 23), besluit.publicatiedatum)
        assertEquals(LocalDate.of(2019, 2, 24), besluit.verzenddatum)
        assertEquals(LocalDate.of(2019, 2, 25), besluit.uiterlijkeReactiedatum)

    }

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

    private fun <T> jsonPathMissingOrNull(jsonPath: String): Matcher<T> {
        return CoreMatchers.anyOf(
            JsonPathMatchers.hasNoJsonPath(jsonPath),
            hasJsonPath(jsonPath, CoreMatchers.nullValue())
        )
    }

    class TestAuthentication : BesluitenApiAuthentication {
        override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
            val filteredRequest = ClientRequest.from(request).headers { headers ->
                headers.setBearerAuth("test")
            }.build()
            return next.exchange(filteredRequest)
        }
    }
}