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

package com.ritense.objectenapi.web.rest

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.objectenapi.service.ZaakObjectService
import com.ritense.objecttypenapi.client.Objecttype
import com.ritense.plugin.web.rest.PluginDefinitionResource
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.UUID

internal class ZaakObjectResourceTest {

    lateinit var mockMvc: MockMvc
    lateinit var zaakObjectService: ZaakObjectService
    lateinit var zaakObjectResource: ZaakObjectResource

    @BeforeEach
    fun init() {
        zaakObjectService = mock()
        zaakObjectResource = ZaakObjectResource(zaakObjectService)

        mockMvc = MockMvcBuilders
            .standaloneSetup(zaakObjectResource)
            .build()
    }

    @Test
    fun `should get objecttypes for documentId`() {
        val documentId = UUID.randomUUID()

        val type1 = mock<Objecttype>()
        whenever(type1.url).thenReturn(URI("http://example.com/1"))
        whenever(type1.name).thenReturn("name 1")

        val type2 = mock<Objecttype>()
        whenever(type2.url).thenReturn(URI("http://example.com/2"))
        whenever(type2.name).thenReturn("name 2")

        whenever(zaakObjectService.getZaakObjectTypes(documentId)).thenReturn(listOf(type1, type2))

        mockMvc
            .perform(
                get("/api/document/$documentId/zaak/objecttype")
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