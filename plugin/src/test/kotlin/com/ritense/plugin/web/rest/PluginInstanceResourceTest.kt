package com.ritense.plugin.web.rest

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.json.Mapper
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.nio.charset.StandardCharsets

internal class PluginInstanceResourceTest {

    lateinit var mockMvc: MockMvc
    lateinit var pluginService: PluginService
    lateinit var pluginInstanceResource: PluginInstanceResource

    @BeforeEach
    fun init() {
        pluginService = mock()
        pluginInstanceResource = PluginInstanceResource(pluginService)

        mockMvc = MockMvcBuilders
            .standaloneSetup(pluginInstanceResource)
            .build()
    }

    @Test
    fun `should get plugin configurations`() {
        val plugin = PluginDefinition("key", "title", "description", "className")
        val plugin2 = PluginDefinition("key2", "title2", "description2", "className2")
        val pluginConfiguration = PluginConfiguration(PluginConfigurationId.newId(), "title", "description", plugin)
        val pluginConfiguration2 = PluginConfiguration(PluginConfigurationId.newId(), "title2", "description2", plugin2)
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
                jsonPath("$.[0].properties").value("description"))
            .andExpect(
                jsonPath("$.[1].properties").value("description2"))
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
        val plugin = PluginDefinition("key", "title", "description", "className")
        val pluginConfiguration = PluginConfiguration(PluginConfigurationId.newId(), "title", "properties", plugin)
        whenever(pluginService.createPluginConfiguration(any(), any(), any())).thenReturn(pluginConfiguration)

        val pluginConfiguratieDto = com.ritense.plugin.web.rest.dto.PluginConfiguration(
            "key",
            "title",
            "properties",
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
                jsonPath("$.properties").value("properties"))
            .andExpect(
                jsonPath("$.pluginDefinition.key").value("key"))
            .andExpect(
                jsonPath("$..pluginDefinition.title").value("title"))
            .andExpect(
                jsonPath("$.pluginDefinition.description").value("description"))
            .andExpect(
                jsonPath("$.pluginDefinition.fullyQualifiedClassName").doesNotExist())
    }
}