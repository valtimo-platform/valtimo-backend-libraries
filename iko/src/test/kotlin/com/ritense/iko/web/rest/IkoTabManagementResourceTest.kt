/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.iko.web.rest

import com.ritense.iko.service.IkoTabService
import com.ritense.iko.web.rest.request.IkoTabCreateRequest
import com.ritense.iko.web.rest.request.IkoTabUpdateRequest
import com.ritense.tab.domain.Tab
import com.ritense.valtimo.contract.json.MapperSingleton
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional

@Transactional
internal class IkoTabManagementResourceTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var resource: IkoTabManagementResource
    private lateinit var service: IkoTabService

    private val objectMapper = MapperSingleton.get()

    @BeforeEach
    fun init() {
        service = mock()
        resource = IkoTabManagementResource(service)
        mockMvc = MockMvcBuilders.standaloneSetup(resource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(MappingJackson2HttpMessageConverter(MapperSingleton.get()))
            .build();
    }

    @Test
    fun `should get tabs`() {
        whenever(service.findAllTabsByIkoDataAggregateKey("klant"))
            .thenReturn(listOf(tab()))

        mockMvc.perform(get("/api/management/v1/iko-data-aggregate/{dataAggregateKey}/tab", "klant"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].key").value("overview"))
            .andExpect(jsonPath("$[0].title").value("Overview"))
            .andExpect(jsonPath("$[0].type").value("widgets"))
    }

    @Test
    fun `should get tab by key`() {
        whenever(service.getByKey("klant", "overview"))
            .thenReturn(tab())

        mockMvc.perform(
            get(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/tab/{tabKey}",
                "klant",
                "overview"
            )
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value("overview"))
            .andExpect(jsonPath("$.title").value("Overview"))
            .andExpect(jsonPath("$.type").value("widgets"))
    }

    @Test
    fun `should create tab`() {
        val tab = tab()
        val request = IkoTabCreateRequest(
            tab.title,
            tab.type,
        )
        whenever(service.create(eq("klant"), any())).thenReturn(tab)

        mockMvc.perform(
            post(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/tab/{tabKey}",
                "klant",
                "overview"
            )
                .content(objectMapper.writeValueAsString(request))
                .contentType(APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value("overview"))
            .andExpect(jsonPath("$.title").value("Overview"))
            .andExpect(jsonPath("$.type").value("widgets"))
    }

    @Test
    fun `should update tab`() {
        val tab = tab()
        val request = IkoTabUpdateRequest(
                tab.key,
                tab.title,
                tab.type,
        )
        whenever(service.findByKey("klant", "overview"))
            .thenReturn(tab)
        whenever(service.update(eq("klant"), any())).thenReturn(tab)
        mockMvc.perform(
            put(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/tab/{key}",
                "klant",
                "overview"
            )
                .content(objectMapper.writeValueAsString(request))
                .contentType(APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value("overview"))
            .andExpect(jsonPath("$.title").value("Overview"))
            .andExpect(jsonPath("$.type").value("widgets"))
    }

    @Test
    fun `should update tab order`() {
        val tab = tab()
        val request = listOf(
            IkoTabUpdateRequest(
                tab.key,
                tab.title,
                tab.type,
            )
        )
        whenever(service.findAllTabsByIkoDataAggregateKey("klant"))
            .thenReturn(listOf(tab))
        whenever(service.update(eq("klant"), any())).thenReturn(tab)
        mockMvc.perform(
            put(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/tab",
                "klant"
            )
                .content(objectMapper.writeValueAsString(request))
                .contentType(APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].key").value("overview"))
            .andExpect(jsonPath("$[0].title").value("Overview"))
            .andExpect(jsonPath("$[0].type").value("widgets"))
    }

    @Test
    fun `should delete tab`() {
        mockMvc.perform(
            delete(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/tab/{tabKey}",
                "klant",
                "overview"
            )
        )
            .andDo(print())
            .andExpect(status().isNoContent())
    }

    private fun tab() = Tab(
        key = "overview",
        title = "Overview",
        type = "widgets",
        order = 0,
    )
}
