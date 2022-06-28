package com.ritense.plugin.web.rest

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.plugin.domain.ActivityType
import com.ritense.plugin.domain.PluginActionDefinition
import com.ritense.plugin.domain.PluginActionDefinitionId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.service.PluginService
import com.ritense.plugin.web.rest.dto.PluginActionDefinitionDto
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.nio.charset.StandardCharsets

internal class PluginDefinitionResourceTest {

    lateinit var mockMvc: MockMvc
    lateinit var pluginService: PluginService
    lateinit var pluginDefinitionResource: PluginDefinitionResource

    @BeforeEach
    fun init() {
        pluginService = mock()
        pluginDefinitionResource = PluginDefinitionResource(pluginService)

        mockMvc = MockMvcBuilders
            .standaloneSetup(pluginDefinitionResource)
            .build()
    }

    @Test
    fun `should get plugin definitions without providing activityType`() {
        val plugin = PluginDefinition("key", "title", "description", "className")
        val plugin2 = PluginDefinition("key2", "title2", "description2", "className2")
        whenever(pluginService.getPluginDefinitions()).thenReturn(listOf(plugin, plugin2))

        mockMvc.perform(get("/api/plugin/definition")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.*", hasSize<Int>(2)))
            .andExpect(jsonPath("$.[0].key").value("key"))
            .andExpect(jsonPath("$.[1].key").value("key2"))
            .andExpect(jsonPath("$.[0].title").value("title"))
            .andExpect(jsonPath("$.[1].title").value("title2"))
            .andExpect(jsonPath("$.[0].description").value("description"))
            .andExpect(jsonPath("$.[1].description").value("description2"))
            .andExpect(jsonPath("$.[0].fullyQualifiedClassName").doesNotExist())
            .andExpect(jsonPath("$.[1].fullyQualifiedClassName").doesNotExist())
    }

    @Test
    fun `should get plugin action definitions`() {
        val actions = listOf(
            PluginActionDefinitionDto(
                "some-key",
                "title",
                "description"
            ),
            PluginActionDefinitionDto(
                "some-other-key",
                "other-title",
                "other-description"
            )
        )
        whenever(pluginService.getPluginDefinitionActions("test", null)).thenReturn(actions)

        mockMvc.perform(get("/api/plugin/definition/test/action")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.*", hasSize<Int>(2)))
            .andExpect(jsonPath("$.[0].key").value("some-key"))
            .andExpect(jsonPath("$.[1].key").value("some-other-key"))
            .andExpect(jsonPath("$.[0].title").value("title"))
            .andExpect(jsonPath("$.[1].title").value("other-title"))
            .andExpect(jsonPath("$.[0].description").value("description"))
            .andExpect(jsonPath("$.[1].description").value("other-description"))
    }
}