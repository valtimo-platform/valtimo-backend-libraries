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
import com.ritense.plugin.BaseIntegrationTest
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.plugin.domain.PluginProcessLinkId
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import com.ritense.plugin.repository.PluginProcessLinkRepository
import com.ritense.plugin.web.rest.request.PluginProcessLinkCreateDto
import com.ritense.plugin.web.rest.request.PluginProcessLinkUpdateDto
import java.nio.charset.StandardCharsets
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext


internal class PluginProcessLinkResourceIT : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    lateinit var pluginConfigurationRepository: PluginConfigurationRepository

    @Autowired
    lateinit var pluginDefinitionRepository: PluginDefinitionRepository

    @Autowired
    lateinit var pluginProcessLinkRepository: PluginProcessLinkRepository


    lateinit var mockMvc: MockMvc
    lateinit var pluginConfiguration: PluginConfiguration

    @BeforeEach
    fun init() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()

        val pluginDefinition = pluginDefinitionRepository.getById("test-plugin")

        pluginConfiguration = pluginConfigurationRepository.save(
            PluginConfiguration(
                PluginConfigurationId.newId(),
                "some-config",
                null,
                pluginDefinition
            )
        )
    }

    @Test
    @Transactional
    fun `should create a process-link`() {
        val createDto = PluginProcessLinkCreateDto(
            processDefinitionId = PROCESS_DEF_ID,
            activityId = ACTIVITY_ID,
            pluginConfigurationId = pluginConfiguration.id.id,
            pluginActionDefinitionKey = ACTION_KEY
        )

        mockMvc.perform(
            post("/api/process-link")
                .content(ObjectMapper().writeValueAsString(createDto))
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isNoContent)
    }

    @Test
    @Transactional
    fun `should list process-links`() {
        createProcessLink()

        mockMvc.perform(
            get("/api/process-link")
                .param("processDefinitionId", PROCESS_DEF_ID)
                .param("activityId", ACTIVITY_ID)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.*", hasSize<Int>(1)))
            .andExpect(jsonPath("$[0].pluginActionDefinitionKey").value("test-action"))
    }

    @Test
    @Transactional
    fun `should update a process-link`() {
        val processLink = createProcessLink()

        val updateDto = PluginProcessLinkUpdateDto(
            id = processLink.id,
            pluginConfigurationId = pluginConfiguration.id.id,
            pluginActionDefinitionKey = "test-action1"
        )

        mockMvc.perform(
            put("/api/process-link")
                .content(ObjectMapper().writeValueAsString(updateDto))
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isNoContent)
    }

    private fun createProcessLink(): PluginProcessLinkId {
        return pluginProcessLinkRepository.save(
            PluginProcessLink(
                PluginProcessLinkId.newId(),
                processDefinitionId = PROCESS_DEF_ID,
                activityId = ACTIVITY_ID,
                pluginConfigurationId = pluginConfiguration.id,
                pluginActionDefinitionKey = ACTION_KEY
            )
        ).id
    }

    companion object {
        const val PROCESS_DEF_ID = "test-process"
        const val ACTIVITY_ID = "test-activity"
        const val ACTION_KEY = "test-action"
    }
}