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

package com.ritense.zakenapi.web.rest

import com.ritense.documentenapi.client.DocumentInformatieObject
import com.ritense.zakenapi.BaseIntegrationTest
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import org.hamcrest.Matchers
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

class ZaakDocumentResourceTest: BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun init() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()
    }

    @Test
    fun `should get zaak-documenten by document`() {
        val documentId = UUID.randomUUID()

        val informatieObjectId = UUID.randomUUID()
        val informatieObject = createDocumentInformatieObject(URI("https://example.local/$informatieObjectId"))
        doReturn(listOf(
            informatieObject
        )).whenever(zaakDocumentService).getInformatieObjecten(documentId)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/zaken-api/document/{documentId}/files", documentId)
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.*", hasSize<Int>(1)))
            .andExpect(jsonPath("$.[0].fileUrl").value(informatieObject.url.toString()))
            .andExpect(jsonPath("$.[0].fileName").value(Matchers.nullValue()))
            .andExpect(jsonPath("$.[0].sizeInBytes").value(informatieObject.bestandsomvang))
            .andExpect(jsonPath("$.[0].createdOn").value(startsWith(informatieObject.creatiedatum.atStartOfDay().toString())))
            .andExpect(jsonPath("$.[0].createdBy").value(informatieObject.auteur))
            .andExpect(jsonPath("$.[0].fileId").value(informatieObjectId.toString()))
    }

    private fun createDocumentInformatieObject(uri: URI) = DocumentInformatieObject(
        url = uri,
        bronorganisatie = "x",
        auteur = "y",
        beginRegistratie = LocalDateTime.now(),
        creatiedatum = LocalDate.now(),
        taal = "nl",
        titel = "titel",
        versie = 1,
        bestandsomvang = 1337L
    )
}