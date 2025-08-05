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

import com.ritense.iko.IkoServerRepository.Companion.ENDPOINT_QUERY_PARAMETERS
import com.ritense.iko.domain.IkoDataAggregate
import com.ritense.iko.domain.IkoDataRequest
import com.ritense.iko.domain.IkoDataRequestId
import com.ritense.iko.service.IkoDataAggregateService
import com.ritense.iko.service.IkoDataRequestService
import com.ritense.iko.web.rest.request.IkoDataRequestCreateRequest
import com.ritense.iko.web.rest.request.IkoDataRequestUpdateRequest
import com.ritense.valtimo.contract.iko.PropertyField
import com.ritense.valtimo.contract.iko.PropertyField.Companion.PROPERTY_FIELD_TYPE_TEXT
import com.ritense.valtimo.contract.json.MapperSingleton
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
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
internal class IkoDataRequestManagementResourceTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var resource: IkoDataRequestManagementResource
    private lateinit var service: IkoDataRequestService
    private lateinit var ikoDataAggregateService: IkoDataAggregateService

    private val objectMapper = MapperSingleton.get()

    @BeforeEach
    fun init() {
        service = mock()
        ikoDataAggregateService = mock()
        resource = IkoDataRequestManagementResource(service, ikoDataAggregateService)
        mockMvc = MockMvcBuilders.standaloneSetup(resource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(MappingJackson2HttpMessageConverter(MapperSingleton.get()))
            .build();
    }

    @Test
    fun `should get iko dataRequest property fields`() {
        whenever(service.getIkoDataRequestPropertyFields("iko")).thenReturn(
            listOf(
                PropertyField(
                    key = ENDPOINT_QUERY_PARAMETERS,
                    type = PROPERTY_FIELD_TYPE_TEXT
                )
            )
        )

        mockMvc.perform(get("/api/management/v1/iko-property-fields/{type}/data-request", "iko"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.*", hasSize<Int>(1)))
            .andExpect(jsonPath("$[0].title").value("Endpoint Query Parameters"))
            .andExpect(jsonPath("$[0].key").value("endpointQueryParameters"))
            .andExpect(jsonPath("$[0].type").value("text"))
    }

    @Test
    fun `should get iko dataRequests`() {
        whenever(service.findAll(isNull(), eq("klant"), isNull())).thenReturn(
            listOf(
                IkoDataRequest(
                    id = IkoDataRequestId("bsn", IkoDataAggregate("klant", "Klant", emptyMap(), mock())),
                    title = "BSN",
                    order = 0,
                    properties = mapOf("endpointQueryParameters" to mapOf("type" to "RaadpleegMetBurgerservicenummer")),
                )
            ),
        )

        mockMvc.perform(get("/api/management/v1/iko-data-aggregate/{dataAggregateKey}/data-request", "klant"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].key").value("bsn"))
            .andExpect(jsonPath("$[0].title").value("BSN"))
    }

    @Test
    fun `should get iko dataRequest by key`() {
        whenever(service.getByKey("bsn", "klant")).thenReturn(
            IkoDataRequest(
                id = IkoDataRequestId("bsn", IkoDataAggregate("klant", "Klant", emptyMap(), mock())),
                title = "BSN",
                order = 0,
                properties = mapOf("endpointQueryParameters" to mapOf("type" to "RaadpleegMetBurgerservicenummer"))
            )
        )

        mockMvc.perform(
            get(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/data-request/{dataRequestKey}",
                "klant",
                "bsn"
            )
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value("bsn"))
            .andExpect(jsonPath("$.title").value("BSN"))
            .andExpect(jsonPath("$.properties.endpointQueryParameters.type").value("RaadpleegMetBurgerservicenummer"))
    }

    @Test
    fun `should create iko dataRequest`() {
        val ikoDataAggregate = IkoDataAggregate("klant", "Klant", emptyMap(), mock())
        val ikoDataRequest = IkoDataRequest(
            id = IkoDataRequestId("bsn", ikoDataAggregate),
            title = "BSN",
            order = 0,
            properties = mapOf("endpointQueryParameters" to mapOf("type" to "RaadpleegMetBurgerservicenummer")),
        )
        val request = IkoDataRequestCreateRequest(
            ikoDataRequest.title,
            ikoDataRequest.properties
        )
        whenever(ikoDataAggregateService.getByKey("klant")).thenReturn(ikoDataAggregate)
        whenever(service.create(any())).thenReturn(ikoDataRequest)

        mockMvc.perform(
            post(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/data-request/{dataRequestKey}",
                "klant",
                "bsn"
            )
                .content(objectMapper.writeValueAsString(request))
                .contentType(APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value("bsn"))
            .andExpect(jsonPath("$.title").value("BSN"))
            .andExpect(jsonPath("$.properties.endpointQueryParameters.type").value("RaadpleegMetBurgerservicenummer"))
    }

    @Test
    fun `should update iko dataRequest`() {
        val ikoDataRequest = IkoDataRequest(
            id = IkoDataRequestId("bsn", IkoDataAggregate("klant", "Klant", emptyMap(), mock())),
            title = "BSN",
            order = 0,
            properties = mapOf("endpointQueryParameters" to mapOf("type" to "RaadpleegMetBurgerservicenummer")),
        )
        val request = IkoDataRequestUpdateRequest(
            ikoDataRequest.id.key,
            ikoDataRequest.id.ikoDataAggregate.key,
            ikoDataRequest.title,
            ikoDataRequest.properties
        )
        whenever(service.getByKey("bsn", "klant")).thenReturn(ikoDataRequest)
        whenever(service.update(any())).thenReturn(ikoDataRequest)
        mockMvc.perform(
            put(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/data-request/{key}",
                "klant",
                "bsn",
            )
                .content(objectMapper.writeValueAsString(request))
                .contentType(APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value("bsn"))
            .andExpect(jsonPath("$.ikoDataAggregateKey").value("klant"))
            .andExpect(jsonPath("$.title").value("BSN"))
            .andExpect(jsonPath("$.properties.endpointQueryParameters.type").value("RaadpleegMetBurgerservicenummer"))
    }

    @Test
    fun `should update iko dataRequests`() {
        val ikoDataRequest = IkoDataRequest(
            id = IkoDataRequestId("bsn", IkoDataAggregate("klant", "Klant", emptyMap(), mock())),
            title = "BSN",
            order = 0,
            properties = mapOf("endpointQueryParameters" to mapOf("type" to "RaadpleegMetBurgerservicenummer")),
        )
        val request = listOf(
            IkoDataRequestUpdateRequest(
                ikoDataRequest.id.key,
                ikoDataRequest.id.ikoDataAggregate.key,
                ikoDataRequest.title,
                ikoDataRequest.properties
            )
        )
        whenever(service.findAll(ikoDataAggregateKey = "klant")).thenReturn(listOf(ikoDataRequest))
        whenever(service.update(any())).thenReturn(ikoDataRequest)
        mockMvc.perform(
            put(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/data-request",
                "klant"
            )
                .content(objectMapper.writeValueAsString(request))
                .contentType(APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].key").value("bsn"))
            .andExpect(jsonPath("$[0].ikoDataAggregateKey").value("klant"))
            .andExpect(jsonPath("$[0].title").value("BSN"))
            .andExpect(jsonPath("$[0].properties.endpointQueryParameters.type").value("RaadpleegMetBurgerservicenummer"))
    }

    @Test
    fun `should delete iko dataRequest`() {
        mockMvc.perform(
            delete(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/data-request/{dataRequestKey}",
                "klant",
                "bsn"
            )
        )
            .andDo(print())
            .andExpect(status().isNoContent())
    }

}
