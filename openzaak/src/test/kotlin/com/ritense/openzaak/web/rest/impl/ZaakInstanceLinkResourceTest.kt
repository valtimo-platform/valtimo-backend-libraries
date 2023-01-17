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

package com.ritense.openzaak.web.rest.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLink
import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLinkId
import com.ritense.openzaak.service.impl.ZaakInstanceLinkService
import com.ritense.openzaak.web.rest.ZaakInstanceLinkResource
import com.ritense.openzaak.web.rest.response.ZaakInstanceLinkDTO
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.UUID

internal class ZaakInstanceLinkResourceTest {

    lateinit var mockMvc: MockMvc
    lateinit var zaakInstanceLinkService: ZaakInstanceLinkService
    lateinit var zaakInstanceLinkResource: ZaakInstanceLinkResource
    lateinit var applicationEventPublisher: ApplicationEventPublisher

    @BeforeEach
    fun init() {
        zaakInstanceLinkService = Mockito.mock(ZaakInstanceLinkService::class.java)
        zaakInstanceLinkResource = ZaakInstanceLinkResource(zaakInstanceLinkService)
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher::class.java)

        mockMvc = MockMvcBuilders
            .standaloneSetup(zaakInstanceLinkResource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .build()
    }

    @Test
    fun `should return ZaakInstaceLink for given zaakInstanceUrl`() {
        // given:
        val url = URI("http://localhost:8001/zaken/api/v1/zaken/df64a38e-566a-409d-8275-9207f70f79e7")
        val documentId = UUID.randomUUID()

        val entity = ZaakInstanceLink(
            zaakInstanceLinkId = ZaakInstanceLinkId(
                id = UUID.randomUUID()
            ),
            zaakInstanceUrl = url,
            zaakInstanceId = UUID.randomUUID(),
            documentId = documentId,
            zaakTypeUrl = URI("http://example.com"),
        )

        //when:
        whenever(zaakInstanceLinkService.getByZaakInstanceUrl(url)).thenReturn(
            entity
        )

        //then:
        val expectedDto = ZaakInstanceLinkDTO(
            zaakInstanceUrl = url,
            documentId = documentId
        )

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/zaakinstancelink/zaak?zaakInstanceUrl=$url")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(
                MockMvcResultMatchers.content().json(
                    jacksonObjectMapper().writeValueAsString(expectedDto)
                )
            )
    }

    @Test
    fun `should return ZaakInstaceLink for given documentId`() {
        // given:
        val url = URI("http://example1.com")
        val documentId = UUID.randomUUID()

        val result = ZaakInstanceLink(
            zaakInstanceLinkId = ZaakInstanceLinkId(
                id = UUID.randomUUID()
            ),
            zaakInstanceUrl = URI("http://example1.com"),
            zaakInstanceId = UUID.randomUUID(),
            documentId = documentId,
            zaakTypeUrl = URI("http://example2.com"),
        )

        //when:
        whenever(zaakInstanceLinkService.getByDocumentId(documentId)).thenReturn(
            result
        )

        //then:
        val expectedDto = ZaakInstanceLinkDTO(
            zaakInstanceUrl = url,
            documentId = documentId
        )

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/zaakinstancelink/document?documentId=$documentId")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(
                MockMvcResultMatchers.content().json(
                    jacksonObjectMapper().writeValueAsString(expectedDto)
                )
            )

    }

}
