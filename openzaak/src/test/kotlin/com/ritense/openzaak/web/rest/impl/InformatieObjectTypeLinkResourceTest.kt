/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.openzaak.domain.mapping.impl.InformatieObjectTypeLink
import com.ritense.openzaak.domain.mapping.impl.InformatieObjectTypeLinkId
import com.ritense.openzaak.service.impl.InformatieObjectTypeLinkService
import com.ritense.openzaak.service.impl.result.CreateInformatieObjectTypeLinkResultFailed
import com.ritense.openzaak.service.impl.result.CreateInformatieObjectTypeLinkResultSucceeded
import com.ritense.openzaak.web.rest.request.CreateInformatieObjectTypeLinkRequest
import com.ritense.valtimo.contract.json.Mapper
import com.ritense.valtimo.contract.result.OperationError
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.UUID

internal class InformatieObjectTypeLinkResourceTest {

    lateinit var mockMvc: MockMvc
    lateinit var informatieObjectTypeLinkResource: InformatieObjectTypeLinkResource
    lateinit var informatieObjectTypeLinkService: InformatieObjectTypeLinkService
    lateinit var createInformatieObjectTypeRequest: CreateInformatieObjectTypeLinkRequest
    private val documentDefinitionName = "name"
    lateinit var informatieObjectTypeLink: InformatieObjectTypeLink

    @BeforeEach
    fun setUp() {
        informatieObjectTypeLinkService = mock(InformatieObjectTypeLinkService::class.java)
        informatieObjectTypeLinkResource = InformatieObjectTypeLinkResource(informatieObjectTypeLinkService)
        mockMvc = MockMvcBuilders
            .standaloneSetup(informatieObjectTypeLinkResource)
            .build()
    }

    @Test
    fun `should get 200 with link as result because it exists`() {
        informatieObjectTypeLink = InformatieObjectTypeLink(
            InformatieObjectTypeLinkId.existingId(UUID.randomUUID()),
            documentDefinitionName,
            URI.create("http://zaaktype.com"),
            URI.create("http://informatieobjectyype.com")
        )
        whenever(informatieObjectTypeLinkService.get(eq(documentDefinitionName)))
            .thenReturn(informatieObjectTypeLink)

        mockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/openzaak/informatie-object-type-link/{documentDefinitionName}",
                documentDefinitionName
            )
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.informatieObjectType").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.zaakType").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.documentDefinitionName").isNotEmpty)
    }

    @Test
    fun `should get 204 empty result because link does not exist`() {
        whenever(informatieObjectTypeLinkService.get(eq(documentDefinitionName)))
            .thenReturn(null)

        mockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/openzaak/informatie-object-type-link/{documentDefinitionName}",
                documentDefinitionName
            )
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isNoContent)
    }

    @Test
    fun `should return 200 with new informatie object type link as body`() {
        createInformatieObjectTypeRequest = CreateInformatieObjectTypeLinkRequest(
            documentDefinitionName,
            URI.create("http://zaaktype.com"),
            URI.create("http://informatieobjecttype.com")
        )

        informatieObjectTypeLink = InformatieObjectTypeLink(
            InformatieObjectTypeLinkId.newId(UUID.randomUUID()),
            documentDefinitionName,
            URI.create("http://zaaktype.com"),
            URI.create("http://informatieobjectyype.com")
        )

        whenever(informatieObjectTypeLinkService.create(eq(createInformatieObjectTypeRequest)))
            .thenReturn(CreateInformatieObjectTypeLinkResultSucceeded(informatieObjectTypeLink))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/openzaak/informatie-object-type-link")
                .content(Mapper.INSTANCE.get().writeValueAsString(createInformatieObjectTypeRequest))
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.informatieObjectTypeLink.zaakType").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.informatieObjectTypeLink.informatieObjectType").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.informatieObjectTypeLink.documentDefinitionName").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
    }

    @Test
    fun `should return 400 with errors as body`() {
        createInformatieObjectTypeRequest = CreateInformatieObjectTypeLinkRequest(
            documentDefinitionName,
            URI.create("http://zaaktype.com"),
            URI.create("http://informatieobjecttype.com")
        )

        whenever(informatieObjectTypeLinkService.create(eq(createInformatieObjectTypeRequest)))
            .thenReturn(CreateInformatieObjectTypeLinkResultFailed(listOf(OperationError.FromException(RuntimeException("message")))))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/openzaak/informatie-object-type-link")
                .content(Mapper.INSTANCE.get().writeValueAsString(createInformatieObjectTypeRequest))
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.errors").isNotEmpty)
    }
}