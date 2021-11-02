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

package com.ritense.connector.web.rest.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.ritense.connector.BaseIntegrationTest
import com.ritense.connector.config.SpringHandlerInstantiatorImpl
import com.ritense.connector.domain.ConnectorInstance
import com.ritense.connector.domain.ConnectorInstanceId
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.ConnectorType
import com.ritense.connector.domain.ConnectorTypeId
import com.ritense.connector.impl.NestedObject
import com.ritense.connector.impl.ObjectApiProperties
import com.ritense.connector.service.ConnectorService
import com.ritense.connector.web.rest.request.CreateConnectorInstanceRequest
import com.ritense.connector.web.rest.result.CreateConnectorInstanceResultSucceeded
import com.ritense.valtimo.contract.json.serializer.PageSerializer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.inject.Inject

@Transactional
internal class ConnectorResourceIntTest : BaseIntegrationTest() {

    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var connectorService: ConnectorService

    @Inject
    lateinit var connectorResource: ConnectorResource

    @Inject
    lateinit var springHandlerInstantiatorImpl: SpringHandlerInstantiatorImpl

    lateinit var mapper: ObjectMapper

    @BeforeEach
    fun init() {
        mapper = JsonMapper.builder()
            .addModule(KotlinModule())
            .build()
        mapper.setHandlerInstantiator(springHandlerInstantiatorImpl)

        mockMvc = MockMvcBuilders
            .standaloneSetup(connectorResource)
            .alwaysDo<StandaloneMockMvcBuilder>(print())
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(jacksonMessageConverter())
            .build()
    }

    @Test
    fun `should get 200 with all connectorTypes in body`() {
        `when`(connectorService.getConnectorTypes()).thenReturn(
            listOf(
                ConnectorType(
                    ConnectorTypeId.newId(UUID.randomUUID()),
                    "aName",
                    "aClassName",
                    ObjectApiProperties()
                ),
                ConnectorType(
                    ConnectorTypeId.newId(UUID.randomUUID()),
                    "aName2",
                    "aClassName2",
                    ObjectApiProperties()
                )
            )
        )

        mockMvc.perform(get("/api/connector/type").accept(APPLICATION_JSON_VALUE))
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `should get 200 with all connectorInstances in body`() {
        val typeId = ConnectorTypeId.newId(UUID.randomUUID())
        val type = ConnectorType(typeId, "name", "class", object: ConnectorProperties{})

        val paged = PageRequest.of(0, 10, Sort.by("name").descending());

        `when`(connectorService.getConnectorInstances(paged)).thenReturn(
            PageImpl(
                listOf(
                    ConnectorInstance(
                        ConnectorInstanceId.newId(UUID.randomUUID()),
                        type,
                        "test1",
                        ObjectApiProperties(NestedObject("aaa"))
                    ),
                    ConnectorInstance(
                        ConnectorInstanceId.newId(UUID.randomUUID()),
                        type,
                        "test2",
                        ObjectApiProperties(NestedObject("bbb"))
                    )
                ),
                Pageable.unpaged(),
                2
            )
        )

        mockMvc.perform(get("/api/connector/instance").accept(APPLICATION_JSON_VALUE))
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$.content.length()").value(2))
    }

    @Test
    fun `should return 200 with new connectorTypeInstance as body`() {

        val request = CreateConnectorInstanceRequest(
            UUID.randomUUID(),
            "cti",
            ObjectApiProperties(NestedObject("myConfiguredProperty"))
        )

        val type = ConnectorType(ConnectorTypeId.existingId(request.typeId), "name", "class", object: ConnectorProperties{})

        `when`(connectorService.createConnectorInstance(request)).thenReturn(
            CreateConnectorInstanceResultSucceeded(
                ConnectorInstance(
                    ConnectorInstanceId.newId(UUID.randomUUID()),
                    type,
                    request.name,
                    request.connectorProperties
                )
            )
        )

        mockMvc.perform(
            post(
                "/api/connector/instance")
                .content(mapper.writeValueAsString(request))
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE)
        )
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$.connectorTypeInstance").isNotEmpty)
            .andExpect(jsonPath("$.connectorTypeInstance.id").isNotEmpty)
            .andExpect(jsonPath("$.connectorTypeInstance.type.id").value(request.typeId.toString()))
            .andExpect(jsonPath("$.connectorTypeInstance.name").value(request.name))
            .andExpect(jsonPath("$.errors").isEmpty)
    }

    private fun jacksonMessageConverter(): MappingJackson2HttpMessageConverter {
        val objectMapper = Jackson2ObjectMapperBuilder()
            .failOnUnknownProperties(false)
            .serializerByType(Page::class.java, PageSerializer()).build<ObjectMapper>()

        objectMapper.setHandlerInstantiator(springHandlerInstantiatorImpl)
        val converter = MappingJackson2HttpMessageConverter()
        converter.objectMapper = objectMapper
        return converter
    }
}

