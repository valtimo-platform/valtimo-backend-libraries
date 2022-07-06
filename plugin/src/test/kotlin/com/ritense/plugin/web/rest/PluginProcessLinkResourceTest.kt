package com.ritense.plugin.web.rest

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.ritense.plugin.service.PluginService
import com.ritense.plugin.web.rest.dto.PluginProcessLinkDto
import com.ritense.valtimo.contract.json.Mapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.nio.charset.StandardCharsets
import java.util.UUID

internal class PluginProcessLinkResourceTest {

    lateinit var mockMvc: MockMvc
    lateinit var pluginService: PluginService
    lateinit var pluginProcessLinkResource: PluginProcessLinkResource

    @BeforeEach
    fun init() {
        pluginService = mock()
        pluginProcessLinkResource = PluginProcessLinkResource(pluginService)

        mockMvc = MockMvcBuilders
            .standaloneSetup(pluginProcessLinkResource)
            .build()
    }


    @Test
    fun `should list plugin process links`() {
        TODO()
    }

    @Test
    fun `should add plugin process link`() {
        val properties: JsonNode = ObjectMapper().readTree("{\"name\": \"whatever\" }")

        val pluginProcessLinkDto = PluginProcessLinkDto(
            processDefinitionId = UUID.randomUUID().toString(),
            pluginConfigurationKey = "some-plugin-configuration",
            activityId = "someActivity",
            pluginActionDefinitionKey = "some-plugin-action",
            actionProperties = properties
        )

        mockMvc.perform(
            post("/api/process-link/plugin")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(Mapper.INSTANCE.get().writeValueAsString(pluginProcessLinkDto))
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isNoContent)

        verify(pluginService).createProcessLink(pluginProcessLinkDto)
    }

    @Test
    fun `should update plugin process link`() {
        TODO()
    }
}