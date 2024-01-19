/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.service.PluginService
import com.ritense.plugin.web.rest.converter.StringToActivityTypeConverter
import com.ritense.plugin.web.rest.request.CreatePluginConfigurationDto
import com.ritense.plugin.web.rest.request.UpdatePluginConfigurationDto
import com.ritense.valtimo.contract.json.MapperSingleton
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.format.support.FormattingConversionService
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
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

        val formattingConversionService = FormattingConversionService()
        formattingConversionService.addConverter(StringToActivityTypeConverter())

        mockMvc = MockMvcBuilders
            .standaloneSetup(pluginConfigurationResource)
            .setConversionService(formattingConversionService)
            .build()
    }

    @Test
    fun `should get plugin configurations`() {
        val properties1: ObjectNode = MapperSingleton.get().readTree("{\"name\": \"whatever\" }") as ObjectNode
        val properties2: ObjectNode = MapperSingleton.get().readTree("{\"other\": \"something\" }") as ObjectNode
        val plugin = PluginDefinition("key", "title", "description", "className")
        val plugin2 = PluginDefinition("key2", "title2", "description2", "className2")
        val pluginConfiguration = PluginConfiguration(PluginConfigurationId.newId(), "title", properties1, plugin)
        val pluginConfiguration2 = PluginConfiguration(PluginConfigurationId.newId(), "title2", properties2, plugin2)
        whenever(pluginService.getPluginConfigurations(any())).thenReturn(listOf(pluginConfiguration, pluginConfiguration2))

        mockMvc.perform(get("/api/v1/plugin/configuration")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .assertConfigurationListOutput()

        verify(pluginService).getPluginConfigurations(any())
    }

    @Test
    fun `should get plugin configurations by category`() {
        val properties1: ObjectNode = MapperSingleton.get().readTree("{\"name\": \"whatever\" }") as ObjectNode
        val properties2: ObjectNode = MapperSingleton.get().readTree("{\"other\": \"something\" }") as ObjectNode
        val plugin = PluginDefinition("key", "title", "description", "className")
        val plugin2 = PluginDefinition("key2", "title2", "description2", "className2")
        val pluginConfiguration = PluginConfiguration(PluginConfigurationId.newId(), "title", properties1, plugin)
        val pluginConfiguration2 = PluginConfiguration(PluginConfigurationId.newId(), "title2", properties2, plugin2)
        whenever(pluginService
            .getPluginConfigurations(any()))
            .thenReturn(listOf(pluginConfiguration, pluginConfiguration2))

        mockMvc.perform(get("/api/v1/plugin/configuration?category=some-category")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .assertConfigurationListOutput()
    }

    @Test
    fun `should filter on plugins for activityType`() {
        whenever(pluginService.getPluginConfigurations(any())).thenReturn(listOf())

        mockMvc.perform(get("/api/v1/plugin/configuration?activityType=bpmn:ServiceTask")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `should not filter on plugins for rare activityType`() {
        whenever(pluginService.getPluginConfigurations(any())).thenReturn(listOf())

        mockMvc.perform(get("/api/v1/plugin/configuration?activityType=bpmn:IntermediateLinkCatch")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `should respond with 400 bad request when filtering on non existing activityType`() {
        whenever(pluginService.getPluginConfigurations(any())).thenReturn(listOf())

        mockMvc.perform(get("/api/v1/plugin/configuration?activityType=bpmn:ActivityTypeDoesntExist")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isBadRequest)
    }

    private fun ResultActions.assertConfigurationListOutput() {
        this.andExpect(status().is2xxSuccessful)
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
        val properties: ObjectNode = MapperSingleton.get().readTree("{\"name\": \"whatever\" }") as ObjectNode
        val plugin = PluginDefinition("key", "title", "description", "className")
        val pluginConfiguration = PluginConfiguration(PluginConfigurationId.newId(), "title", properties, plugin)

        whenever(pluginService.createPluginConfiguration(any(), any(), any(), any())).thenReturn(pluginConfiguration)

        val pluginConfiguratieDto = CreatePluginConfigurationDto(
            "title",
            properties,
            "key",
            UUID.fromString("3ab43f1a-0154-4658-82b8-41527def0aee")
        )

        mockMvc.perform(
            post("/api/v1/plugin/configuration")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(MapperSingleton.get().writeValueAsString(pluginConfiguratieDto))
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
            PluginConfigurationId.existingId(UUID.fromString("3ab43f1a-0154-4658-82b8-41527def0aee")),
            "title",
            properties,
            "key")
    }

    @Test
    fun `should update plugin configuration`() {
        val properties: ObjectNode = MapperSingleton.get().readTree("{\"name\": \"whatever\" }") as ObjectNode
        val plugin = PluginDefinition("key", "title", "description", "className")
        val pluginConfigurationId = UUID.randomUUID()
        val pluginConfiguration = PluginConfiguration(PluginConfigurationId.existingId(pluginConfigurationId), "title", properties, plugin)
        whenever(pluginService.updatePluginConfiguration(any(), any(), any(), any())).thenReturn(pluginConfiguration)

        val pluginConfiguratieDto = UpdatePluginConfigurationDto(
            "title",
            properties
        )

        mockMvc.perform(
            put("/api/v1/plugin/configuration/$pluginConfigurationId")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(MapperSingleton.get().writeValueAsString(pluginConfiguratieDto))
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
            PluginConfigurationId.existingId(pluginConfigurationId),
            "title",
            properties)
    }

    @Test
    fun `should delete plugin configuration`() {
        val pluginConfigurationId = UUID.randomUUID()

        mockMvc.perform(
            delete("/api/v1/plugin/configuration/$pluginConfigurationId")
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
