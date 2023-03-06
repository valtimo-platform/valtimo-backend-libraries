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

import com.ritense.openzaak.service.impl.ZaakService
import com.ritense.openzaak.service.impl.model.catalogi.InformatieObjectType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.UUID

internal class InformatieObjectTypeResourceTest {

    lateinit var mockMvc: MockMvc
    lateinit var zaakService: ZaakService
    lateinit var informatieObjectTypeResource: InformatieObjectTypeResource
    lateinit var informatieObjectType: InformatieObjectType

    @BeforeEach
    fun init() {
        zaakService = mock(ZaakService::class.java)
        informatieObjectType = mock(InformatieObjectType::class.java)

        informatieObjectTypeResource = InformatieObjectTypeResource(zaakService)
        mockMvc = MockMvcBuilders.standaloneSetup(informatieObjectTypeResource).build()
    }

    @Test
    fun `should get 200 with all available informatie object types in body`() {
        val catalogus = UUID.randomUUID()
        whenever(zaakService.getInformatieobjecttypes(eq(catalogus)))
            .thenReturn(listOf(informatieObjectType))

        mockMvc.perform(get("/api/v1/openzaak/informatie-object-typen/{catalogus}", catalogus)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
    }
}