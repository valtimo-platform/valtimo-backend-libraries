package com.ritense.plugin.web.rest

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.plugin.service.PluginService
import com.ritense.plugin.web.rest.dto.processlink.PluginProcessLinkCreateDto
import com.ritense.plugin.web.rest.dto.processlink.PluginProcessLinkResultDto
import com.ritense.plugin.web.rest.dto.processlink.PluginProcessLinkUpdateDto
import com.ritense.valtimo.contract.json.Mapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
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
        val properties: JsonNode = ObjectMapper().readTree("{\"name\": \"whatever\" }")
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val processDefinitionId = "pid"
        val activityId = "aid"

        val processLinks = listOf(
            PluginProcessLinkResultDto(
                id = id1,
                processDefinitionId = processDefinitionId,
                activityId = activityId,
                actionProperties = properties,
                pluginConfigurationKey = "pluginConfigurationKey1",
                pluginActionDefinitionKey = "pluginActionDefinitionKey1"
            ),
            PluginProcessLinkResultDto(
                id = id2,
                processDefinitionId = processDefinitionId,
                activityId = activityId,
                actionProperties = properties,
                pluginConfigurationKey = "pluginConfigurationKey2",
                pluginActionDefinitionKey = "pluginActionDefinitionKey2"
            )
        )

        whenever(pluginService.getProcessLinks(any(), any())).thenReturn(processLinks)

        mockMvc.perform(
            get("/api/process-link?processDefinitionId=$processDefinitionId&activityId=$activityId")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.[0].id").value(id1.toString()))
            .andExpect(jsonPath("$.[0].processDefinitionId").value(processDefinitionId))
            .andExpect(jsonPath("$.[0].activityId").value(activityId))
            .andExpect(jsonPath("$.[0].actionProperties.name").value("whatever"))
            .andExpect(jsonPath("$.[0].pluginConfigurationKey").value("pluginConfigurationKey1"))
            .andExpect(jsonPath("$.[0].pluginActionDefinitionKey").value("pluginActionDefinitionKey1"))
            .andExpect(jsonPath("$.[1].id").value(id2.toString()))
            .andExpect(jsonPath("$.[1].processDefinitionId").value(processDefinitionId))
            .andExpect(jsonPath("$.[1].activityId").value(activityId))
            .andExpect(jsonPath("$.[1].actionProperties.name").value("whatever"))
            .andExpect(jsonPath("$.[1].pluginConfigurationKey").value("pluginConfigurationKey2"))
            .andExpect(jsonPath("$.[1].pluginActionDefinitionKey").value("pluginActionDefinitionKey2"))


        verify(pluginService).getProcessLinks(processDefinitionId, activityId)
    }

    @Test
    fun `should add plugin process link`() {
        val properties: JsonNode = ObjectMapper().readTree("{\"name\": \"whatever\" }")

        val pluginProcessLinkDto = PluginProcessLinkCreateDto(
            processDefinitionId = UUID.randomUUID().toString(),
            pluginConfigurationKey = "some-plugin-configuration",
            activityId = "someActivity",
            pluginActionDefinitionKey = "some-plugin-action",
            actionProperties = properties
        )

        mockMvc.perform(
            post("/api/process-link")
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
        val properties: JsonNode = ObjectMapper().readTree("{\"name\": \"whatever\" }")

        val pluginProcessLinkDto = PluginProcessLinkUpdateDto(
            id = UUID.randomUUID(),
            pluginConfigurationKey = "some-plugin-configuration",
            pluginActionDefinitionKey = "some-plugin-action",
            actionProperties = properties
        )

        mockMvc.perform(
            put("/api/process-link")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(Mapper.INSTANCE.get().writeValueAsString(pluginProcessLinkDto))
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isNoContent)

        verify(pluginService).updateProcessLink(pluginProcessLinkDto)
    }
}