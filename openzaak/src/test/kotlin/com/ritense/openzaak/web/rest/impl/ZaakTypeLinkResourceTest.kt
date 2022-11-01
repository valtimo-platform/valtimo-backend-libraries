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

import com.ritense.openzaak.domain.mapping.impl.Operation
import com.ritense.openzaak.domain.mapping.impl.ServiceTaskHandler
import com.ritense.openzaak.domain.mapping.impl.ServiceTaskHandlers
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLink
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLinkId
import com.ritense.openzaak.service.impl.ZaakTypeLinkService
import com.ritense.openzaak.service.impl.result.CreateServiceTaskHandlerResultSucceeded
import com.ritense.openzaak.service.impl.result.ModifyServiceTaskHandlerResultSucceeded
import com.ritense.openzaak.web.rest.request.ServiceTaskHandlerRequest
import com.ritense.valtimo.contract.json.Mapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.UUID

internal class ZaakTypeLinkResourceTest {

    lateinit var mockMvc: MockMvc
    lateinit var zaakTypeLinkService: ZaakTypeLinkService
    lateinit var zaakTypeLinkResource: ZaakTypeLinkResource
    lateinit var applicationEventPublisher: ApplicationEventPublisher

    @BeforeEach
    fun init() {
        zaakTypeLinkService = Mockito.mock(ZaakTypeLinkService::class.java)
        zaakTypeLinkResource = ZaakTypeLinkResource(zaakTypeLinkService)
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher::class.java)

        mockMvc = MockMvcBuilders
            .standaloneSetup(zaakTypeLinkResource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .build()
    }

    @Test
    fun `should create service task handler`() {
        val id = UUID.randomUUID()
        val zaaktypeLinkId = ZaakTypeLinkId.existingId(id)
        val serviceTaskHandler = ServiceTaskHandler("processKey", "taskId", Operation.SET_STATUS, URI.create("http://example.com"))
        val serviceTaskHandlers = ServiceTaskHandlers()
        serviceTaskHandlers.add(serviceTaskHandler)

        val zaaktypeLink = ZaakTypeLink(
            zaaktypeLinkId,
            "documentDefinitionName",
            URI.create("zaakTypeUrl"),
            serviceTaskHandlers
        )

        val serviceTaskHandlerRequest = ServiceTaskHandlerRequest("processKey", "taskId", Operation.SET_STATUS, URI.create("http://example.com"))

        whenever(zaakTypeLinkService.assignServiceTaskHandler(eq(zaaktypeLinkId), eq(serviceTaskHandlerRequest)))
            .thenReturn(CreateServiceTaskHandlerResultSucceeded(zaaktypeLink))

        mockMvc.perform(post("/api/openzaak/link/{id}/service-handler", id)
            .content(Mapper.INSTANCE.get().writeValueAsString(serviceTaskHandlerRequest))
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
    }

    @Test
    fun `should update service task handler`() {
        val id = UUID.randomUUID()
        val zaaktypeLinkId = ZaakTypeLinkId.existingId(id)
        val serviceTaskHandler = ServiceTaskHandler("processKey", "taskId", Operation.SET_STATUS, URI.create("http://example.com"))
        val serviceTaskHandlers = ServiceTaskHandlers()
        serviceTaskHandlers.add(serviceTaskHandler)

        val zaaktypeLink = ZaakTypeLink(
            zaaktypeLinkId,
            "documentDefinitionName",
            URI.create("zaakTypeUrl"),
            serviceTaskHandlers
        )

        val serviceTaskHandlerRequest = ServiceTaskHandlerRequest("processKey", "taskId", Operation.SET_STATUS, URI.create("http://example.com"))
        whenever(zaakTypeLinkService.modifyServiceTaskHandler(eq(zaaktypeLinkId), eq(serviceTaskHandlerRequest)))
            .thenReturn(ModifyServiceTaskHandlerResultSucceeded(zaaktypeLink))

        mockMvc.perform(put("/api/openzaak/link/{id}/service-handler", id)
            .content(Mapper.INSTANCE.get().writeValueAsString(serviceTaskHandlerRequest))
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
    }

}