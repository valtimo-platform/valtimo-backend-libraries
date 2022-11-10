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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.plugin.service.PluginService
import com.ritense.plugin.web.rest.request.PluginProcessLinkCreateDto
import com.ritense.plugin.web.rest.request.PluginProcessLinkUpdateDto
import com.ritense.plugin.web.rest.result.PluginProcessLinkResultDto
import com.ritense.valtimo.contract.json.Mapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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
        val properties: ObjectNode = ObjectMapper().readTree("{\"name\": \"whatever\" }") as ObjectNode
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val processDefinitionId = "pid"
        val activityId = "aid"

        val pluginConfigurationId1 = UUID.randomUUID()
        val pluginConfigurationId2 = UUID.randomUUID()

        val processLinks = listOf(
            PluginProcessLinkResultDto(
                id = id1,
                processDefinitionId = processDefinitionId,
                activityId = activityId,
                actionProperties = properties,
                pluginConfigurationId = pluginConfigurationId1,
                pluginActionDefinitionKey = "pluginActionDefinitionKey1"
            ),
            PluginProcessLinkResultDto(
                id = id2,
                processDefinitionId = processDefinitionId,
                activityId = activityId,
                actionProperties = properties,
                pluginConfigurationId = pluginConfigurationId2,
                pluginActionDefinitionKey = "pluginActionDefinitionKey2"
            )
        )

        whenever(pluginService.getProcessLinks(any(), any())).thenReturn(processLinks)

        mockMvc.perform(
            get("/api/v1/process-link?processDefinitionId=$processDefinitionId&activityId=$activityId")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.[0].id").value(id1.toString()))
            .andExpect(jsonPath("$.[0].processDefinitionId").value(processDefinitionId))
            .andExpect(jsonPath("$.[0].activityId").value(activityId))
            .andExpect(jsonPath("$.[0].actionProperties.name").value("whatever"))
            .andExpect(jsonPath("$.[0].pluginConfigurationId").value(pluginConfigurationId1.toString()))
            .andExpect(jsonPath("$.[0].pluginActionDefinitionKey").value("pluginActionDefinitionKey1"))
            .andExpect(jsonPath("$.[1].id").value(id2.toString()))
            .andExpect(jsonPath("$.[1].processDefinitionId").value(processDefinitionId))
            .andExpect(jsonPath("$.[1].activityId").value(activityId))
            .andExpect(jsonPath("$.[1].actionProperties.name").value("whatever"))
            .andExpect(jsonPath("$.[1].pluginConfigurationId").value(pluginConfigurationId2.toString()))
            .andExpect(jsonPath("$.[1].pluginActionDefinitionKey").value("pluginActionDefinitionKey2"))


        verify(pluginService).getProcessLinks(processDefinitionId, activityId)
    }

    @Test
    fun `should add plugin process link`() {
        val properties: ObjectNode = ObjectMapper().readTree("{\"name\": \"whatever\" }") as ObjectNode

        val pluginProcessLinkDto = PluginProcessLinkCreateDto(
            processDefinitionId = UUID.randomUUID().toString(),
            pluginConfigurationId = UUID.randomUUID(),
            activityId = "someActivity",
            pluginActionDefinitionKey = "some-plugin-action",
            actionProperties = properties
        )

        mockMvc.perform(
            post("/api/v1/process-link")
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
        val properties: ObjectNode = ObjectMapper().readTree("{\"name\": \"whatever\" }") as ObjectNode

        val pluginProcessLinkDto = PluginProcessLinkUpdateDto(
            id = UUID.randomUUID(),
            pluginConfigurationId = UUID.randomUUID(),
            pluginActionDefinitionKey = "some-plugin-action",
            actionProperties = properties
        )

        mockMvc.perform(
            put("/api/v1/process-link")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(Mapper.INSTANCE.get().writeValueAsString(pluginProcessLinkDto))
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isNoContent)

        verify(pluginService).updateProcessLink(pluginProcessLinkDto)
    }

    @Test
    fun `should delete plugin process link`() {
        val pluginProcessLinkId = UUID.randomUUID()

        mockMvc.perform(delete("/api/v1/process-link/{processLinkId}", pluginProcessLinkId))
            .andDo(print())
            .andExpect(status().isNoContent)

        verify(pluginService).deleteProcessLink(pluginProcessLinkId)
    }
}
