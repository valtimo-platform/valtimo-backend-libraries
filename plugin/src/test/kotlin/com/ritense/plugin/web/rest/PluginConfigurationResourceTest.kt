/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.plugin.web.rest

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.service.PluginService
import com.ritense.plugin.web.rest.request.CreatePluginConfigurationDto
import com.ritense.plugin.web.rest.request.UpdatePluginConfigurationDto
import com.ritense.plugin.web.rest.result.PluginConfigurationDto
import com.ritense.valtimo.contract.json.Mapper
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.nio.charset.StandardCharsets
import java.util.UUID

internal class PluginConfigurationResourceTest {

    lateinit var mockMvc: MockMvc
    lateinit var pluginService: PluginService
    lateinit var pluginConfigurationResource: PluginConfigurationResource

    @BeforeEach
    fun init() {
        pluginService = mock()
        pluginConfigurationResource = PluginConfigurationResource(pluginService)

        mockMvc = MockMvcBuilders
            .standaloneSetup(pluginConfigurationResource)
            .build()
    }

    @Test
    fun `should get plugin configurations`() {
        val properties1: JsonNode = ObjectMapper().readTree("{\"name\": \"whatever\" }")
        val properties2: JsonNode = ObjectMapper().readTree("{\"other\": \"something\" }")
        val plugin = PluginDefinition("key", "title", "description", "className")
        val plugin2 = PluginDefinition("key2", "title2", "description2", "className2")
        val pluginConfiguration = PluginConfiguration(PluginConfigurationId.newId(), "title", properties1, plugin)
        val pluginConfiguration2 = PluginConfiguration(PluginConfigurationId.newId(), "title2", properties2, plugin2)
        whenever(pluginService.getPluginConfigurations()).thenReturn(listOf(pluginConfiguration, pluginConfiguration2))

        mockMvc.perform(get("/api/plugin/configuration")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(
                jsonPath("$").isNotEmpty)
            .andExpect(
                jsonPath("$").isArray)
            .andExpect(
                jsonPath("$.*", hasSize<Int>(2)))
            .andExpect(
                jsonPath("$.[0].id").exists())
            .andExpect(
                jsonPath("$.[1].id").exists())
            .andExpect(
                jsonPath("$.[0].key").doesNotExist())
            .andExpect(
                jsonPath("$.[1].key").doesNotExist())
            .andExpect(
                jsonPath("$.[0].title").value("title"))
            .andExpect(
                jsonPath("$.[1].title").value("title2"))
            .andExpect(
                jsonPath("$.[0].properties.name").value("whatever"))
            .andExpect(
                jsonPath("$.[1].properties.other").value("something"))
            .andExpect(
                jsonPath("$.[0].pluginDefinition.key").value("key"))
            .andExpect(
                jsonPath("$.[1].pluginDefinition.key").value("key2"))
            .andExpect(
                jsonPath("$.[0].pluginDefinition.title").value("title"))
            .andExpect(
                jsonPath("$.[1].pluginDefinition.title").value("title2"))
            .andExpect(
                jsonPath("$.[0].pluginDefinition.description").value("description"))
            .andExpect(
                jsonPath("$.[1].pluginDefinition.description").value("description2"))
            .andExpect(
                jsonPath("$.[0].pluginDefinition.fullyQualifiedClassName").doesNotExist())
            .andExpect(
                jsonPath("$.[1].pluginDefinition.fullyQualifiedClassName").doesNotExist())
    }

    @Test
    fun `should save plugin configuration`() {
        val properties: JsonNode = ObjectMapper().readTree("{\"name\": \"whatever\" }")
        val plugin = PluginDefinition("key", "title", "description", "className")
        val pluginConfiguration = PluginConfiguration(PluginConfigurationId.newId(), "title", properties, plugin)

        whenever(pluginService.createPluginConfiguration(any(), any(), any())).thenReturn(pluginConfiguration)

        val pluginConfiguratieDto = CreatePluginConfigurationDto(
            "title",
            properties,
            "key"
        )

        mockMvc.perform(
            post("/api/plugin/configuration")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(Mapper.INSTANCE.get().writeValueAsString(pluginConfiguratieDto))
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(
                jsonPath("$.id").exists())
            .andExpect(
                jsonPath("$.key").doesNotExist())
            .andExpect(
                jsonPath("$.title").value("title"))
            .andExpect(
                jsonPath("$.properties.name").value("whatever"))
            .andExpect(
                jsonPath("$.pluginDefinition.key").value("key"))
            .andExpect(
                jsonPath("$..pluginDefinition.title").value("title"))
            .andExpect(
                jsonPath("$.pluginDefinition.description").value("description"))
            .andExpect(
                jsonPath("$.pluginDefinition.fullyQualifiedClassName").doesNotExist())

        verify(pluginService).createPluginConfiguration(
            "title",
            properties,
            "key")
    }

    @Test
    fun `should update plugin configuration`() {
        val properties: JsonNode = ObjectMapper().readTree("{\"name\": \"whatever\" }")
        val plugin = PluginDefinition("key", "title", "description", "className")
        val pluginConfigurationId = UUID.randomUUID()
        val pluginConfiguration = PluginConfiguration(PluginConfigurationId.existingId(pluginConfigurationId), "title", properties, plugin)
        whenever(pluginService.updatePluginConfiguration(any(), any(), any())).thenReturn(pluginConfiguration)

        val pluginConfiguratieDto = UpdatePluginConfigurationDto(
            "title",
            properties
        )

        mockMvc.perform(
            put("/api/plugin/configuration/$pluginConfigurationId")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(Mapper.INSTANCE.get().writeValueAsString(pluginConfiguratieDto))
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(
                jsonPath("$.id").value(pluginConfigurationId.toString()))
            .andExpect(
                jsonPath("$.title").value("title"))
            .andExpect(
                jsonPath("$.properties.name").value("whatever"))
            .andExpect(
                jsonPath("$.pluginDefinition.key").value("key"))
            .andExpect(
                jsonPath("$..pluginDefinition.title").value("title"))
            .andExpect(
                jsonPath("$.pluginDefinition.description").value("description"))
            .andExpect(
                jsonPath("$.pluginDefinition.fullyQualifiedClassName").doesNotExist())

        verify(pluginService).updatePluginConfiguration(
            PluginConfigurationId.existingId(pluginConfigurationId),
            "title",
            properties)
    }

    @Test
    fun `should delete plugin configuration`() {
        val pluginConfigurationId = UUID.randomUUID()

        mockMvc.perform(
            delete("/api/plugin/configuration/$pluginConfigurationId")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isNoContent)

        verify(pluginService).deletePluginConfiguration(
            PluginConfigurationId.existingId(pluginConfigurationId)
        )
    }
}