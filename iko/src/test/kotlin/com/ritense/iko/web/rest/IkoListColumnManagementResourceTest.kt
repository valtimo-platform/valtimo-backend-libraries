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

import com.ritense.iko.service.IkoListColumnService
import com.ritense.iko.web.rest.request.IkoListColumnCreateRequest
import com.ritense.iko.web.rest.request.IkoListColumnUpdateRequest
import com.ritense.search.domain.DisplayType
import com.ritense.search.domain.SearchListColumn
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
internal class IkoListColumnManagementResourceTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var resource: IkoListColumnManagementResource
    private lateinit var service: IkoListColumnService

    private val objectMapper = MapperSingleton.get()

    @BeforeEach
    fun init() {
        service = mock()
        resource = IkoListColumnManagementResource(service)
        mockMvc = MockMvcBuilders.standaloneSetup(resource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(MappingJackson2HttpMessageConverter(MapperSingleton.get()))
            .build();
    }

    @Test
    fun `should get listColumns`() {
        whenever(service.findAllColumnsByIkoDataAggregateKey("klant"))
            .thenReturn(listOf(listColumn()))

        mockMvc.perform(get("/api/management/v1/iko-data-aggregate/{dataAggregateKey}/column", "klant"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].key").value("naam"))
            .andExpect(jsonPath("$[0].title").value("Naam"))
            .andExpect(jsonPath("$[0].path").value("/naam/volledigeNaam"))
            .andExpect(jsonPath("$[0].displayType.type").value("text"))
            .andExpect(jsonPath("$[0].sortable").value("false"))
    }

    @Test
    fun `should get listColumn by key`() {
        whenever(service.getByKey("klant", "naam"))
            .thenReturn(listColumn())

        mockMvc.perform(
            get(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/column/{listColumnKey}",
                "klant",
                "naam"
            )
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value("naam"))
            .andExpect(jsonPath("$.title").value("Naam"))
            .andExpect(jsonPath("$.path").value("/naam/volledigeNaam"))
            .andExpect(jsonPath("$.displayType.type").value("text"))
            .andExpect(jsonPath("$.sortable").value("false"))
    }

    @Test
    fun `should create listColumn`() {
        val listColumn = listColumn()
        val request = IkoListColumnCreateRequest(
            listColumn.key,
            listColumn.title,
            listColumn.path,
            listColumn.order,
            listColumn.displayType,
            listColumn.sortable,
            listColumn.defaultSort,
        )
        whenever(service.create(eq("klant"), any())).thenReturn(listColumn)

        mockMvc.perform(
            post(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/column/{listColumnKey}",
                "klant",
                "naam"
            )
                .content(objectMapper.writeValueAsString(request))
                .contentType(APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value("naam"))
            .andExpect(jsonPath("$.title").value("Naam"))
            .andExpect(jsonPath("$.path").value("/naam/volledigeNaam"))
            .andExpect(jsonPath("$.displayType.type").value("text"))
            .andExpect(jsonPath("$.sortable").value("false"))
    }

    @Test
    fun `should update listColumn`() {
        val listColumn = listColumn()
        val request = listOf(
            IkoListColumnUpdateRequest(
                listColumn.key,
                listColumn.title,
                listColumn.path,
                listColumn.order,
                listColumn.displayType,
                listColumn.sortable,
                listColumn.defaultSort,
            )
        )
        whenever(service.findAllColumnsByIkoDataAggregateKey("klant"))
            .thenReturn(listOf(listColumn()))
        whenever(service.update(eq("klant"), any())).thenReturn(listColumn)
        mockMvc.perform(
            put(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/column",
                "klant",
                "naam"
            )
                .content(objectMapper.writeValueAsString(request))
                .contentType(APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].key").value("naam"))
            .andExpect(jsonPath("$[0].title").value("Naam"))
            .andExpect(jsonPath("$[0].path").value("/naam/volledigeNaam"))
            .andExpect(jsonPath("$[0].displayType.type").value("text"))
            .andExpect(jsonPath("$[0].sortable").value("false"))
    }

    @Test
    fun `should delete listColumn`() {
        mockMvc.perform(
            delete(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/column/{listColumnKey}",
                "klant",
                "naam"
            )
        )
            .andDo(print())
            .andExpect(status().isNoContent())
    }

    private fun listColumn() = SearchListColumn(
        ownerId = "ownerId",
        key = "naam",
        title = "Naam",
        path = "/naam/volledigeNaam",
        order = 0,
        displayType = DisplayType(),
        sortable = false,
    )
}
