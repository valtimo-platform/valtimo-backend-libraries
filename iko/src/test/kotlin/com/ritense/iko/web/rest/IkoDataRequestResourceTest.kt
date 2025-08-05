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

import com.ritense.iko.domain.IkoDataAggregate
import com.ritense.iko.domain.IkoDataRequest
import com.ritense.iko.domain.IkoDataRequestId
import com.ritense.iko.service.IkoDataRequestService
import com.ritense.iko.service.IkoListColumnService
import com.ritense.iko.service.IkoSearchFieldService
import com.ritense.iko.web.rest.request.IkoSearchRequest
import com.ritense.search.domain.DataType
import com.ritense.search.domain.DisplayType
import com.ritense.search.domain.EmptyDisplayTypeParameter
import com.ritense.search.domain.FieldType
import com.ritense.search.domain.SearchFieldV2
import com.ritense.search.domain.SearchListColumn
import com.ritense.valtimo.contract.json.MapperSingleton
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional

@Transactional
internal class IkoDataRequestResourceTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var resource: IkoDataRequestResource
    private lateinit var ikoDataRequestService: IkoDataRequestService
    private lateinit var ikoListColumnService: IkoListColumnService
    private lateinit var ikoSearchFieldService: IkoSearchFieldService

    private val objectMapper = MapperSingleton.get()

    @BeforeEach
    fun init() {
        ikoDataRequestService = mock()
        ikoListColumnService = mock()
        ikoSearchFieldService = mock()
        resource = IkoDataRequestResource(ikoDataRequestService, ikoListColumnService, ikoSearchFieldService)
        mockMvc = MockMvcBuilders.standaloneSetup(resource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(MappingJackson2HttpMessageConverter(MapperSingleton.get()))
            .build();
    }

    @Test
    fun `should get iko dataRequests`() {
        val ikoDataRequest = IkoDataRequest(
            id = IkoDataRequestId("bsn", IkoDataAggregate("klant", "Klant", emptyMap(), mock())),
            title = "BSN",
            order = 0,
            properties = mapOf("endpointQueryParameters" to mapOf("type" to "RaadpleegMetBurgerservicenummer")),
        )
        whenever(ikoDataRequestService.findAll(isNull(), eq("klant"), isNull()))
            .thenReturn(listOf(ikoDataRequest))
        whenever(ikoSearchFieldService.findAllSearchFieldsByIkoDataRequest("klant", ikoDataRequest.id.key))
            .thenReturn(
                listOf(
                    SearchFieldV2(
                        ownerId = "klant:bsn",
                        ownerType = "IkoDataRequest",
                        key = "bsn",
                        title = "BSN",
                        path = "/burgerservicenummer",
                        order = 0,
                        dataType = DataType.TEXT,
                        fieldType = FieldType.SINGLE,
                    )
                )
            )

        mockMvc.perform(get("/api/v1/iko-data-aggregate/{dataAggregateKey}/data-request", "klant"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].key").value("bsn"))
            .andExpect(jsonPath("$[0].title").value("BSN"))
            .andExpect(jsonPath("$[0].searchFields[0].key").value("bsn"))
            .andExpect(jsonPath("$[0].searchFields[0].title").value("BSN"))
    }

    @Test
    fun `should search iko`() {
        val ikoDataAggregate = IkoDataAggregate("klant", "Klant", emptyMap(), mock())
        val ikoDataRequest = IkoDataRequest(
            id = IkoDataRequestId("bsn", ikoDataAggregate),
            title = "BSN",
            order = 0,
            properties = mapOf("endpointQueryParameters" to mapOf("type" to "RaadpleegMetBurgerservicenummer")),
        )
        val request = IkoSearchRequest(filters = mapOf("bsn" to "000000000"))
        whenever(ikoListColumnService.findAllColumnsByIkoDataAggregateKey(ikoDataAggregate.key)).thenReturn(
            listOf(
                SearchListColumn(
                    ownerId = "IkoDataRequest:klant:bsn",
                    key = "bsn",
                    title = "BSN",
                    path = "/burgerservicenummer",
                    order = 1,
                    displayType = DisplayType("text", EmptyDisplayTypeParameter()),
                    sortable = true,
                )
            )
        )
        whenever(ikoSearchFieldService.findAllSearchFieldsByIkoDataRequest(ikoDataAggregate.key, ikoDataRequest.id.key))
            .thenReturn(
                listOf(
                    SearchFieldV2(
                        ownerId = "klant:bsn",
                        ownerType = "IkoDataRequest",
                        key = "bsn",
                        title = "BSN",
                        path = "/burgerservicenummer",
                        order = 0,
                        dataType = DataType.TEXT,
                        fieldType = FieldType.SINGLE,
                    )
                )
            )
        whenever(ikoDataRequestService.searchData(eq(ikoDataRequest.id.key), eq(ikoDataAggregate.key), any(), any()))
            .thenReturn(PageImpl(listOf(objectMapper.readTree("""{"burgerservicenummer":"000000000","naam":{"voornamen":"John","geslachtsnaam":"Doe","voorletters":"J.","volledigeNaam":"John Doe"}}"""))))


        mockMvc.perform(
            post(
                "/api/v1/iko-data-aggregate/{ikoDataAggregateKey}/data-request/{ikoDataRequestKey}/search",
                "klant",
                "bsn"
            )
                .content(objectMapper.writeValueAsString(request))
                .contentType(APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.headers[0].key").value("bsn"))
            .andExpect(jsonPath("$.headers[0].title").value("BSN"))
            .andExpect(jsonPath("$.rows.content[0].items[0].key").value("bsn"))
            .andExpect(jsonPath("$.rows.content[0].items[0].value").value("000000000"))
    }
}
