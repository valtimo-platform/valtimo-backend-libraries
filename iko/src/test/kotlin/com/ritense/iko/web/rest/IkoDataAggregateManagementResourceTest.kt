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

import com.ritense.iko.IkoServerRepository.Companion.ENDPOINT_PATH
import com.ritense.iko.domain.IkoDataAggregate
import com.ritense.iko.domain.IkoRepositoryConfig
import com.ritense.iko.service.IkoDataAggregateService
import com.ritense.iko.web.rest.request.IkoDataAggregateCreateRequest
import com.ritense.iko.web.rest.request.IkoDataAggregateUpdateRequest
import com.ritense.valtimo.contract.iko.PropertyField
import com.ritense.valtimo.contract.iko.PropertyField.Companion.PROPERTY_FIELD_TYPE_TEXT
import com.ritense.valtimo.contract.json.MapperSingleton
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
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
internal class IkoDataAggregateManagementResourceTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var resource: IkoDataAggregateManagementResource
    private lateinit var service: IkoDataAggregateService

    private val objectMapper = MapperSingleton.get()

    @BeforeEach
    fun init() {
        service = mock()
        resource = IkoDataAggregateManagementResource(service)
        mockMvc = MockMvcBuilders.standaloneSetup(resource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(MappingJackson2HttpMessageConverter(MapperSingleton.get()))
            .build();
    }

    @Test
    fun `should get iko dataAggregate property fields`() {
        whenever(service.getIkoDataAggregatePropertyFields("iko")).thenReturn(
            listOf(
                PropertyField(
                    key = ENDPOINT_PATH,
                    type = PROPERTY_FIELD_TYPE_TEXT
                )
            )
        )

        mockMvc.perform(get("/api/management/v1/iko-property-fields/{type}/data-aggregate", "iko"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.*", hasSize<Int>(1)))
            .andExpect(jsonPath("$[0].title").value("Search Path"))
            .andExpect(jsonPath("$[0].key").value("endpointPath"))
            .andExpect(jsonPath("$[0].type").value("text"))
    }

    @Test
    fun `should get iko dataAggregates`() {
        val pageable = PageRequest.of(0, 10)
        whenever(service.findAll(eq("klant"), eq("Klant"), any())).thenReturn(
            PageImpl(
                listOf(IkoDataAggregate(key = "klant", title = "Klant", ikoRepositoryConfig = mock())),
                pageable,
                1
            )
        )

        mockMvc.perform(get("/api/management/v1/iko-data-aggregate?key={key}&title={title}", "klant", "Klant"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].key").value("klant"))
            .andExpect(jsonPath("$.content[0].title").value("Klant"))
    }

    @Test
    fun `should get iko dataAggregate by key`() {
        whenever(service.getByKey("klant")).thenReturn(
            IkoDataAggregate(
                key = "klant",
                title = "Klant",
                properties = mapOf("endpointPath" to "personen"),
                ikoRepositoryConfig = mock()
            )
        )

        mockMvc.perform(get("/api/management/v1/iko-data-aggregate/{dataAggregateKey}", "klant"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value("klant"))
            .andExpect(jsonPath("$.title").value("Klant"))
            .andExpect(jsonPath("$.properties.endpointPath").value("personen"))
    }

    @Test
    fun `should create iko dataAggregate`() {
        val ikoDataAggregate = IkoDataAggregate(
            key = "klant",
            title = "Klant",
            properties = mapOf("endpointPath" to "personen"),
            ikoRepositoryConfig = IkoRepositoryConfig("iko-api", "IKO API", "iko")
        )
        val request = IkoDataAggregateCreateRequest(
            ikoDataAggregate.ikoRepositoryConfig.key,
            ikoDataAggregate.title,
            ikoDataAggregate.properties
        )
        whenever(
            service.createIkoDataAggregate(
                ikoDataAggregate.key,
                ikoDataAggregate.ikoRepositoryConfig.key,
                ikoDataAggregate.title,
                ikoDataAggregate.properties
            )
        )
            .thenReturn(ikoDataAggregate)

        mockMvc.perform(
            post("/api/management/v1/iko-data-aggregate/{dataAggregateKey}", "klant")
                .content(objectMapper.writeValueAsString(request))
                .contentType(APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value("klant"))
            .andExpect(jsonPath("$.title").value("Klant"))
            .andExpect(jsonPath("$.properties.endpointPath").value("personen"))
    }

    @Test
    fun `should update iko dataAggregate`() {
        val ikoDataAggregate = IkoDataAggregate(
            key = "klant",
            title = "Klant",
            properties = mapOf("endpointPath" to "personen"),
            ikoRepositoryConfig = IkoRepositoryConfig("iko-api", "IKO API", "iko")
        )
        val request = IkoDataAggregateUpdateRequest(
            ikoDataAggregate.ikoRepositoryConfig.key,
            ikoDataAggregate.title,
            ikoDataAggregate.properties
        )
        whenever(
            service.saveIkoDataAggregate(
                ikoDataAggregate.key,
                ikoDataAggregate.ikoRepositoryConfig.key,
                ikoDataAggregate.title,
                ikoDataAggregate.properties
            )
        )
            .thenReturn(ikoDataAggregate)
        mockMvc.perform(
            put("/api/management/v1/iko-data-aggregate/{dataAggregateKey}", "klant")
                .content(objectMapper.writeValueAsString(request))
                .contentType(APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value("klant"))
            .andExpect(jsonPath("$.title").value("Klant"))
            .andExpect(jsonPath("$.properties.endpointPath").value("personen"))
    }

    @Test
    fun `should delete iko dataAggregate`() {
        mockMvc.perform(delete("/api/management/v1/iko-data-aggregate/{dataAggregateKey}", "klant"))
            .andDo(print())
            .andExpect(status().isNoContent())
    }

}
