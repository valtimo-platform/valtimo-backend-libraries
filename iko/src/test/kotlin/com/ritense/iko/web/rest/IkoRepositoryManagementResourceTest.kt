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

import com.ritense.iko.IkoServerRepository.Companion.PLUGIN_CONFIGURATION
import com.ritense.iko.domain.IkoRepositoryConfig
import com.ritense.iko.service.IkoRepositoryService
import com.ritense.iko.web.rest.request.IkoRepositoryConfigCreateRequest
import com.ritense.iko.web.rest.request.IkoRepositoryConfigUpdateRequest
import com.ritense.valtimo.contract.iko.PropertyField
import com.ritense.valtimo.contract.iko.PropertyField.Companion.PROPERTY_FIELD_TYPE_DROPDOWN
import com.ritense.valtimo.contract.json.MapperSingleton
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
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
internal class IkoRepositoryManagementResourceTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var resource: IkoRepositoryManagementResource
    private lateinit var service: IkoRepositoryService

    private val objectMapper = MapperSingleton.get()

    @BeforeEach
    fun init() {
        service = mock()
        resource = IkoRepositoryManagementResource(service)
        mockMvc = MockMvcBuilders.standaloneSetup(resource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(MappingJackson2HttpMessageConverter(MapperSingleton.get()))
            .build();
    }

    @Test
    fun `should get iko repository types`() {
        whenever(service.getIkoRepositoryTypes()).thenReturn(mapOf("iko" to "Iko Server Repository", "objectenApi" to "Objecten Api Iko Repository"))

        mockMvc.perform(get("/api/management/v1/iko-types"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.iko").value("Iko Server Repository"))
            .andExpect(jsonPath("$.objectenApi").value("Objecten Api Iko Repository"))
    }

    @Test
    fun `should get iko repository property fields`() {
        whenever(service.getIkoRepositoryConfigPropertyFields("iko")).thenReturn(
            listOf(
                PropertyField(
                    title = PropertyField.toReadableText(PLUGIN_CONFIGURATION),
                    key = PLUGIN_CONFIGURATION,
                    type = PROPERTY_FIELD_TYPE_DROPDOWN,
                    dropdownList = listOf("1234" to "My Plugin")
                )
            )
        )

        mockMvc.perform(get("/api/management/v1/iko-property-fields/{type}/repository-config", "iko"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.*", hasSize<Int>(1)))
            .andExpect(jsonPath("$[0].title").value("Plugin Configuration"))
            .andExpect(jsonPath("$[0].key").value("pluginConfiguration"))
            .andExpect(jsonPath("$[0].type").value("dropdown"))
            .andExpect(jsonPath("$[0].dropdownList[0].first").value("1234"))
            .andExpect(jsonPath("$[0].dropdownList[0].second").value("My Plugin"))
    }

    @Test
    fun `should get iko repository configs`() {
        val pageable = PageRequest.of(0, 10)
        whenever(service.findAll(isNull(), eq("IKO API"), eq("iko"), any())).thenReturn(
            PageImpl(
                listOf(IkoRepositoryConfig(key = "iko-api", title = "IKO API", type = "iko")),
                pageable,
                1
            )
        )

        mockMvc.perform(get("/api/management/v1/iko?title={title}&type={type}", "IKO API", "iko"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].key").value("iko-api"))
            .andExpect(jsonPath("$.content[0].title").value("IKO API"))
            .andExpect(jsonPath("$.content[0].type").value("iko"))
    }

    @Test
    fun `should get iko repository config by key`() {
        whenever(service.getByKey("iko-api")).thenReturn(
            IkoRepositoryConfig(
                key = "iko-api",
                title = "IKO API",
                type = "iko",
                properties = mapOf("pluginConfiguration" to "b6d83348-97e7-4660-bd35-2e5fcc9629b4")
            )
        )

        mockMvc.perform(get("/api/management/v1/iko/{repositoryConfigKey}", "iko-api"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value("iko-api"))
            .andExpect(jsonPath("$.title").value("IKO API"))
            .andExpect(jsonPath("$.type").value("iko"))
            .andExpect(jsonPath("$.properties.pluginConfiguration").value("b6d83348-97e7-4660-bd35-2e5fcc9629b4"))
    }

    @Test
    fun `should create iko repository config`() {
        val config = IkoRepositoryConfig(
            key = "iko-api",
            title = "IKO API",
            type = "iko",
            properties = mapOf("pluginConfiguration" to "b6d83348-97e7-4660-bd35-2e5fcc9629b4")
        )
        val request = IkoRepositoryConfigCreateRequest(config.title, config.type, config.properties)
        whenever(service.createIkoRepositoryConfig(config.key, config.title, config.type, config.properties))
            .thenReturn(config)

        mockMvc.perform(
            post("/api/management/v1/iko/{repositoryConfigKey}", "iko-api")
                .content(objectMapper.writeValueAsString(request))
                .contentType(APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value("iko-api"))
            .andExpect(jsonPath("$.title").value("IKO API"))
            .andExpect(jsonPath("$.type").value("iko"))
            .andExpect(jsonPath("$.properties.pluginConfiguration").value("b6d83348-97e7-4660-bd35-2e5fcc9629b4"))
    }

    @Test
    fun `should udpate iko repository config`() {
        val config = IkoRepositoryConfig(
            key = "iko-api",
            title = "IKO API",
            type = "iko",
            properties = mapOf("pluginConfiguration" to "b6d83348-97e7-4660-bd35-2e5fcc9629b4")
        )
        val request = IkoRepositoryConfigUpdateRequest(config.title, config.type, config.properties)
        whenever(service.saveIkoRepositoryConfig(config.key, config.title, config.type, config.properties))
            .thenReturn(config)

        mockMvc.perform(
            put("/api/management/v1/iko/{repositoryConfigKey}", "iko-api")
                .content(objectMapper.writeValueAsString(request))
                .contentType(APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value("iko-api"))
            .andExpect(jsonPath("$.title").value("IKO API"))
            .andExpect(jsonPath("$.type").value("iko"))
            .andExpect(jsonPath("$.properties.pluginConfiguration").value("b6d83348-97e7-4660-bd35-2e5fcc9629b4"))
    }

    @Test
    fun `should delete iko repository config`() {
        mockMvc.perform(delete("/api/management/v1/iko/{repositoryConfigKey}", "iko-api"))
            .andDo(print())
            .andExpect(status().isNoContent())
    }

}
