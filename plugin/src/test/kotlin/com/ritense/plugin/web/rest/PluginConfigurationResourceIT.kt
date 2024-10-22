/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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
import com.ritense.plugin.BaseIntegrationTest
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import com.ritense.plugin.service.EncryptionService
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import java.nio.charset.StandardCharsets

@Transactional
internal class PluginConfigurationResourceIT: BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    lateinit var pluginConfigurationRepository: PluginConfigurationRepository

    @Autowired
    lateinit var pluginDefinitionRepository: PluginDefinitionRepository

    @Autowired
    lateinit var encryptionService: EncryptionService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    lateinit var categoryPluginConfiguration: PluginConfiguration
    lateinit var pluginConfiguration: PluginConfiguration
    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun init() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()

        val pluginDefinition = pluginDefinitionRepository.getReferenceById("test-plugin")

        pluginConfiguration = pluginConfigurationRepository.save(
            PluginConfiguration(
                PluginConfigurationId.newId(),
                "some-config",
                objectMapper.readTree(
                    """
                    {
                        "property1": "my-secret",
                        "property2": "my-normal-property"
                    }
                    """
                ) as ObjectNode,
                pluginDefinition,
                encryptionService,
                objectMapper
            )
        )

        val categoryPluginDefinition = pluginDefinitionRepository.getReferenceById("test-category-plugin")
        categoryPluginConfiguration = pluginConfigurationRepository.save(
            PluginConfiguration(
                PluginConfigurationId.newId(),
                "title",
                null,
                categoryPluginDefinition,
                encryptionService,
                objectMapper
            )
        )
    }

    @Test
    fun `should get plugin configurations by category`() {
        mockMvc.perform(get("/api/v1/plugin/configuration?category=test-interface")
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
            .andExpect(jsonPath("$.*", hasSize<Int>(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.[?(@.title=='some-config')]","").exists())
            .andExpect(jsonPath("$.[?(@.title=='some-config')].id").value(pluginConfiguration.id.id.toString()))
    }

    @Test
    fun `should not find get plugin configurations with non-existing category`() {
        mockMvc.perform(get("/api/v1/plugin/configuration?category=some-random-string")
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
    fun `should filter plugin configurations based on activityType`() {
        // Adding another plugin definition (without actions)
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
        mockMvc.perform(get("/api/v1/plugin/configuration?activityType=bpmn:ServiceTask:start")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$.*", hasSize<Int>(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[?(@.title=='some-config')]","").exists())
    }

    @Test
    fun `should create plugin configurations with id`() {
        mockMvc.perform(post("/api/v1/plugin/configuration")
            .content("""
                {
                    "id": "f63997d7-d30e-4a1c-8d16-885e5077c0a2",
                    "title": "Test plugin",
                    "definitionKey": "test-plugin",
                    "properties": {
                        "property1": "test123",
                        "property3": 456,
                        "property4": "${categoryPluginConfiguration.id.id}"
                    }
                }
            """.trimIndent())
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.id").value("f63997d7-d30e-4a1c-8d16-885e5077c0a2"))
            .andExpect(jsonPath("$.title").value("Test plugin"))
            .andExpect(jsonPath("$.pluginDefinition.key").value("test-plugin"))
    }

    @Test
    fun `should export plugin configurations with placeholders`() {
        mockMvc.perform(get("/api/v1/plugin/configuration/export")
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[?(@.title=='some-config')].id","").exists())
            .andExpect(jsonPath("$[?(@.title=='some-config')].title").value("some-config"))
            .andExpect(jsonPath("$[?(@.title=='some-config')].pluginDefinitionKey").value("test-plugin"))
            .andExpect(jsonPath("$[?(@.title=='some-config')].properties.property1").value("\${SOME_CONFIG_PROPERTY1}"))
            .andExpect(jsonPath("$[?(@.title=='some-config')].properties.property2").value("my-normal-property"))
    }
}