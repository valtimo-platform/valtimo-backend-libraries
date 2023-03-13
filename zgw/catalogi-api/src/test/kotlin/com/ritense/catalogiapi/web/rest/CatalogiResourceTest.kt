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

package com.ritense.catalogiapi.web.rest

import com.ritense.catalogiapi.domain.Informatieobjecttype
import com.ritense.catalogiapi.domain.Resultaattype
import com.ritense.catalogiapi.domain.Roltype
import com.ritense.catalogiapi.domain.Statustype
import com.ritense.catalogiapi.service.CatalogiService
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.net.URI
import java.nio.charset.StandardCharsets

internal class CatalogiResourceTest {

    lateinit var mockMvc: MockMvc
    lateinit var catalogiService: CatalogiService
    lateinit var catalogiResource: CatalogiResource

    @BeforeEach
    fun init() {
        catalogiService = mock()
        catalogiResource = CatalogiResource(catalogiService)

        mockMvc = MockMvcBuilders
            .standaloneSetup(catalogiResource)
            .build()
    }

    @Test
    fun `should get documenttypes for documentDefinitionName`() {
        val documentDefinitionName = "case-name"

        val type1 = mock<Informatieobjecttype>()
        whenever(type1.url).thenReturn(URI("http://example.com/1"))
        whenever(type1.omschrijving).thenReturn("name 1")

        val type2 = mock<Informatieobjecttype>()
        whenever(type2.url).thenReturn(URI("http://example.com/2"))
        whenever(type2.omschrijving).thenReturn("name 2")

        whenever(catalogiService.getInformatieobjecttypes(documentDefinitionName))
            .thenReturn(listOf(type1, type2))

        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/documentdefinition/$documentDefinitionName/zaaktype/documenttype")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.*", Matchers.hasSize<Int>(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].url").value("http://example.com/1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[1].url").value("http://example.com/2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].name").value("name 1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[1].name").value("name 2"))
    }

    @Test
    fun `should get roltypes for caseDefinitionName`() {
        val caseDefinitionName = "case-name"

        val type1 = mock<Roltype>()
        whenever(type1.url).thenReturn(URI("http://example.com/1"))
        whenever(type1.omschrijving).thenReturn("name 1")

        val type2 = mock<Roltype>()
        whenever(type2.url).thenReturn(URI("http://example.com/2"))
        whenever(type2.omschrijving).thenReturn("name 2")

        whenever(catalogiService.getRoltypes(caseDefinitionName))
            .thenReturn(listOf(type1, type2))

        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/case-definition/$caseDefinitionName/zaaktype/roltype")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.*", Matchers.hasSize<Int>(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].url").value("http://example.com/1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[1].url").value("http://example.com/2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].name").value("name 1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[1].name").value("name 2"))
    }

    @Test
    fun `should get statustypes for caseDefinitionName`() {
        val caseDefinitionName = "case-name"

        val type1 = mock<Statustype>()
        whenever(type1.url).thenReturn(URI("http://example.com/1"))
        whenever(type1.omschrijving).thenReturn("name 1")

        val type2 = mock<Statustype>()
        whenever(type2.url).thenReturn(URI("http://example.com/2"))
        whenever(type2.omschrijving).thenReturn("name 2")

        whenever(catalogiService.getStatustypes(caseDefinitionName))
            .thenReturn(listOf(type1, type2))

        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/case-definition/$caseDefinitionName/zaaktype/statustype")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.*", Matchers.hasSize<Int>(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].url").value("http://example.com/1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[1].url").value("http://example.com/2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].name").value("name 1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[1].name").value("name 2"))
    }

    @Test
    fun `should get resultaattypes for caseDefinitionName`() {
        val caseDefinitionName = "case-name"

        val type1 = mock<Resultaattype>()
        whenever(type1.url).thenReturn(URI("http://example.com/1"))
        whenever(type1.omschrijving).thenReturn("name 1")

        val type2 = mock<Resultaattype>()
        whenever(type2.url).thenReturn(URI("http://example.com/2"))
        whenever(type2.omschrijving).thenReturn("name 2")

        whenever(catalogiService.getResultaattypes(caseDefinitionName))
            .thenReturn(listOf(type1, type2))

        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/case-definition/$caseDefinitionName/zaaktype/resultaattype")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.*", Matchers.hasSize<Int>(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].url").value("http://example.com/1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[1].url").value("http://example.com/2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].name").value("name 1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[1].name").value("name 2"))
    }

}
