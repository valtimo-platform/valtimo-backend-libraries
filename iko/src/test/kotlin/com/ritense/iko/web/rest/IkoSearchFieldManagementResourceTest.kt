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

import com.ritense.iko.service.IkoSearchFieldService
import com.ritense.iko.web.rest.request.IkoSearchFieldCreateRequest
import com.ritense.iko.web.rest.request.IkoSearchFieldUpdateRequest
import com.ritense.search.domain.DataType
import com.ritense.search.domain.FieldType
import com.ritense.search.domain.SearchFieldMatchType
import com.ritense.search.domain.SearchFieldV2
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
internal class IkoSearchFieldManagementResourceTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var resource: IkoSearchFieldManagementResource
    private lateinit var service: IkoSearchFieldService

    private val objectMapper = MapperSingleton.get()

    @BeforeEach
    fun init() {
        service = mock()
        resource = IkoSearchFieldManagementResource(service)
        mockMvc = MockMvcBuilders.standaloneSetup(resource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(MappingJackson2HttpMessageConverter(MapperSingleton.get()))
            .build();
    }

    @Test
    fun `should get iko searchFields`() {
        whenever(service.findAllSearchFieldsByIkoDataRequest("klant", "bsn")).thenReturn(
            listOf(searchField())
        )

        mockMvc.perform(
            get(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/data-request/{dataRequestKey}/search-field",
                "klant",
                "bsn"
            )
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].key").value("bsn"))
            .andExpect(jsonPath("$[0].title").value("BSN"))
            .andExpect(jsonPath("$[0].path").value("/bsn"))
            .andExpect(jsonPath("$[0].order").value(0))
            .andExpect(jsonPath("$[0].dataType").value("text"))
            .andExpect(jsonPath("$[0].fieldType").value("single"))
            .andExpect(jsonPath("$[0].matchType").value("exact"))
            .andExpect(jsonPath("$[0].required").value(true))
    }

    @Test
    fun `should get iko searchField by key`() {
        whenever(service.getByKey("klant", "bsn", "bsn"))
            .thenReturn(searchField())

        mockMvc.perform(
            get(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/data-request/{dataRequestKey}/search-field/{searchFieldKey}",
                "klant",
                "bsn",
                "bsn"
            )
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value("bsn"))
            .andExpect(jsonPath("$.title").value("BSN"))
            .andExpect(jsonPath("$.path").value("/bsn"))
            .andExpect(jsonPath("$.order").value(0))
            .andExpect(jsonPath("$.dataType").value("text"))
            .andExpect(jsonPath("$.fieldType").value("single"))
            .andExpect(jsonPath("$.matchType").value("exact"))
            .andExpect(jsonPath("$.required").value(true))
    }

    @Test
    fun `should create iko searchField`() {
        val searchField = searchField()
        val request = IkoSearchFieldCreateRequest(
            key = searchField.key,
            title = searchField.title,
            path = searchField.path,
            dataType = searchField.dataType,
            fieldType = searchField.fieldType,
            matchType = searchField.matchType,
            dropdownDataProvider = searchField.dropdownDataProvider,
            required = searchField.required,
        )
        whenever(service.create(eq("klant"), eq("bsn"), any())).thenReturn(searchField)

        mockMvc.perform(
            post(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/data-request/{dataRequestKey}/search-field/{searchFieldKey}",
                "klant",
                "bsn",
                "bsn"
            )
                .content(objectMapper.writeValueAsString(request))
                .contentType(APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value("bsn"))
            .andExpect(jsonPath("$.title").value("BSN"))
            .andExpect(jsonPath("$.path").value("/bsn"))
            .andExpect(jsonPath("$.order").value(0))
            .andExpect(jsonPath("$.dataType").value("text"))
            .andExpect(jsonPath("$.fieldType").value("single"))
            .andExpect(jsonPath("$.matchType").value("exact"))
            .andExpect(jsonPath("$.required").value(true))
    }

    @Test
    fun `should update iko searchField`() {
        val searchField = searchField()
        val request = IkoSearchFieldUpdateRequest(
            key = searchField.key,
            title = searchField.title,
            path = searchField.path,
            dataType = searchField.dataType,
            fieldType = searchField.fieldType,
            matchType = searchField.matchType,
            dropdownDataProvider = searchField.dropdownDataProvider,
            required = searchField.required,
        )
        whenever(service.findByKey("klant", "bsn", "bsn"))
            .thenReturn(searchField)
        whenever(service.update(eq("klant"), eq("bsn"), any()))
            .thenReturn(searchField)
        mockMvc.perform(
            put(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/data-request/{dataRequestKey}/search-field/{key}",
                "klant",
                "bsn",
                "bsn"
            )
                .content(objectMapper.writeValueAsString(request))
                .contentType(APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value("bsn"))
            .andExpect(jsonPath("$.title").value("BSN"))
            .andExpect(jsonPath("$.path").value("/bsn"))
            .andExpect(jsonPath("$.order").value(0))
            .andExpect(jsonPath("$.dataType").value("text"))
            .andExpect(jsonPath("$.fieldType").value("single"))
            .andExpect(jsonPath("$.matchType").value("exact"))
            .andExpect(jsonPath("$.required").value(true))
    }

    @Test
    fun `should update iko searchFields order`() {
        val searchField = searchField()
        val request = listOf(
            IkoSearchFieldUpdateRequest(
                key = searchField.key,
                title = searchField.title,
                path = searchField.path,
                dataType = searchField.dataType,
                fieldType = searchField.fieldType,
                matchType = searchField.matchType,
                dropdownDataProvider = searchField.dropdownDataProvider,
                required = searchField.required,
            )
        )
        whenever(service.findAllSearchFieldsByIkoDataRequest("klant", "bsn")).thenReturn(
            listOf(searchField)
        )
        whenever(service.update(eq("klant"), eq("bsn"), any()))
            .thenReturn(searchField)
        mockMvc.perform(
            put(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/data-request/{dataRequestKey}/search-field",
                "klant",
                "bsn"
            )
                .content(objectMapper.writeValueAsString(request))
                .contentType(APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].key").value("bsn"))
            .andExpect(jsonPath("$[0].title").value("BSN"))
            .andExpect(jsonPath("$[0].path").value("/bsn"))
            .andExpect(jsonPath("$[0].order").value(0))
            .andExpect(jsonPath("$[0].dataType").value("text"))
            .andExpect(jsonPath("$[0].fieldType").value("single"))
            .andExpect(jsonPath("$[0].matchType").value("exact"))
            .andExpect(jsonPath("$[0].required").value(true))
    }

    @Test
    fun `should delete iko searchField`() {
        mockMvc.perform(
            delete(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/data-request/{dataRequestKey}/search-field/{searchFieldKey}",
                "klant",
                "bsn",
                "bsn"
            )
        )
            .andDo(print())
            .andExpect(status().isNoContent())
    }

    private fun searchField() = SearchFieldV2(
        ownerType = "ownerType",
        ownerId = "ownerId",
        key = "bsn",
        title = "BSN",
        path = "/bsn",
        order = 0,
        dataType = DataType.TEXT,
        fieldType = FieldType.SINGLE,
        matchType = SearchFieldMatchType.EXACT,
        dropdownDataProvider = null,
        required = true
    )
}
