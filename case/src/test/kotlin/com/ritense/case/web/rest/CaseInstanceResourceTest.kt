/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.case.web.rest

import com.ritense.case.service.CaseInstanceService
import com.ritense.case.web.rest.dto.CaseListRowDto
import com.ritense.document.domain.search.SearchWithConfigRequest
import com.ritense.valtimo.contract.json.MapperSingleton
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders


class CaseInstanceResourceTest {
    lateinit var mockMvc: MockMvc
    lateinit var resource: CaseInstanceResource
    lateinit var service: CaseInstanceService

    @BeforeEach
    fun setUp() {
        service = mock()
        resource = CaseInstanceResource(service)
        MappingJackson2HttpMessageConverter(MapperSingleton.get())
        mockMvc = MockMvcBuilders.standaloneSetup(resource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(MappingJackson2HttpMessageConverter(MapperSingleton.get()))
            .build()
    }

    @Test
    fun `should get case settings`() {
        val caseDefinitionName = "name"
        val caseListDto = PageImpl(
            listOf(CaseListRowDto("myDocumentId", listOf(CaseListRowDto.CaseListItemDto("createdOn", "2022-12-28"))))
        )

        whenever(service.search(eq(caseDefinitionName), any(), any())).thenReturn(caseListDto)
        val searchRequest = SearchWithConfigRequest()

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/v1/case/{caseDefinitionName}/search", caseDefinitionName)
                    .content(MapperSingleton.get().writeValueAsString(searchRequest))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].items.size()").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].items[0].key").value("createdOn"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].items[0].value").value("2022-12-28"))

        verify(service).search(eq(caseDefinitionName), any(), any())
    }
}
