/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.web.rest.impl

import com.ritense.openzaak.BaseTest
import com.ritense.openzaak.service.impl.ZaakResultaatService
import com.ritense.openzaak.service.impl.model.ResultWrapper
import com.ritense.openzaak.service.impl.model.catalogi.ResultaatType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.net.URI
import java.nio.charset.StandardCharsets

class ResultaatResourceTest : BaseTest() {

    lateinit var mvc: MockMvc
    lateinit var zaakResultaatService: ZaakResultaatService
    lateinit var zaakresultaatResource: ResultaatResource

    @BeforeEach
    fun init() {
        zaakResultaatService = mock(ZaakResultaatService::class.java)
        zaakresultaatResource = ResultaatResource(zaakResultaatService)

        mvc = MockMvcBuilders.standaloneSetup(zaakresultaatResource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .build()
    }

    @Test
    fun getResultaatTypes() {
        val zaakType = URI("http://example.com")

        whenever(zaakResultaatService.getResultaatTypes(eq(zaakType)))
            .thenReturn(ResultWrapper(
                1,
                URI.create("http://example.com"),
                URI.create("http://example.com"),
                listOf(resultaatType())
            ))

        mvc.perform(MockMvcRequestBuilders.post("/api/v1/openzaak/resultaat")
            .content("""{"zaaktype": "http://example.com"}""")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(APPLICATION_JSON_VALUE)
            .accept(APPLICATION_JSON_VALUE))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
    }

    private fun resultaatType(): ResultaatType {
        val brondatumArchiefprocedure = ResultaatType.BrondatumArchiefprocedure(
            ResultaatType.Afleidingswijze.afgehandeld,
            "datumkenmerk",
            true,
            "adres",
            "registratie",
            "procestermijn"
        )
        val resultaatType = ResultaatType(
            URI("http://example.com"),
            URI("http://example.com"),
            "omschrijving",
            URI("http://example.com"),
            "omschrijvingGeneriek",
            URI("http://example.com"),
            "toelichting",
            ResultaatType.Archiefnominatie.blijvend_bewaren,
            "archiefactietermijn",
            brondatumArchiefprocedure
        )
        return resultaatType
    }

}