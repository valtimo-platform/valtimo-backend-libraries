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

import com.ritense.plugin.BaseIntegrationTest
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.nio.charset.StandardCharsets
import javax.transaction.Transactional

@Transactional
internal class PluginConfigurationResourceIT: BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    lateinit var pluginConfigurationRepository: PluginConfigurationRepository

    @Autowired
    lateinit var pluginDefinitionRepository: PluginDefinitionRepository

    lateinit var pluginConfiguration: PluginConfiguration
    lateinit var mockMvc: MockMvc

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
    fun `should get plugin configurations by category`() {
        mockMvc.perform(get("/api/plugin/configuration?category=test-interface")
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
                jsonPath("$.*", hasSize<Int>(1)))
            .andExpect(jsonPath("$[0].title").value("some-config"))
            .andExpect(jsonPath("$[0].id").value(pluginConfiguration.id.id.toString()))
    }

    @Test
    fun `should not find get plugin configurations with non-existing category`() {
        mockMvc.perform(get("/api/plugin/configuration?category=some-random-string")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(
                jsonPath("$").isArray)
            .andExpect(
                jsonPath("$.*", hasSize<Int>(0)))
    }

    @Test
    fun `should filter plugin configurations by action`() {
        // Addding another plugin definition (without actions)
        val pluginDefinition = PluginDefinition("key", "title", "description", "class")
        pluginDefinitionRepository.save(pluginDefinition)
        pluginConfiguration = pluginConfigurationRepository.save(
            PluginConfiguration(
                PluginConfigurationId.newId(),
                "some-config-without-action",
                null,
                pluginDefinition
            )
        )

        // assert that the new plugin configuration is not included in the result
        mockMvc.perform(get("/api/plugin/configuration?includeActionless=false")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$.*", hasSize<Int>(1)))
            .andExpect(jsonPath("$[0].title").value("some-config"))
    }
}