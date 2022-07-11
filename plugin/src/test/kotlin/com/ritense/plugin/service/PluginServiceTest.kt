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

package com.ritense.plugin.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.plugin.domain.ActivityType
import com.ritense.plugin.domain.PluginActionDefinition
import com.ritense.plugin.domain.PluginActionDefinitionId
import com.ritense.plugin.repository.PluginActionDefinitionRepository
import com.ritense.plugin.PluginFactory
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProperty
import com.ritense.plugin.domain.PluginPropertyId
import com.ritense.plugin.exception.PluginPropertyParseException
import com.ritense.plugin.exception.PluginPropertyRequiredException
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows

internal class PluginServiceTest {

    lateinit var pluginDefinitionRepository: PluginDefinitionRepository
    lateinit var pluginConfigurationRepository: PluginConfigurationRepository
    lateinit var pluginActionDefinitionRepository: PluginActionDefinitionRepository
    lateinit var pluginFactory: PluginFactory<Any>
    lateinit var pluginService: PluginService

    @BeforeEach
    fun init() {
        pluginDefinitionRepository = mock()
        pluginConfigurationRepository = mock()
        pluginActionDefinitionRepository = mock()
        pluginFactory = mock()
        pluginService = PluginService(
            pluginDefinitionRepository,
            pluginConfigurationRepository,
            pluginActionDefinitionRepository,
            listOf(pluginFactory)
        )
    }

    @Test
    fun `should get plugin definitions from repository`(){
        pluginService.getPluginDefinitions()
        verify(pluginDefinitionRepository).findAll()
    }

    @Test
    fun `should get plugin configurations from repository`(){
        pluginService.getPluginConfigurations()
        verify(pluginConfigurationRepository).findAll()
    }

    @Test
    fun `should save plugin configuration`(){
        val pluginDefinition = newPluginDefinition()
        addPluginProperty(pluginDefinition)
        newPluginConfiguration(pluginDefinition)

        pluginService
            .createPluginConfiguration(
                "title", ObjectMapper().readTree("{\"name\": \"whatever\" }"), "key"
            )
        verify(pluginConfigurationRepository).save(any())
    }

    @Test
    fun `should throw exception when required plugin property field is missing`() {
        val pluginDefinition = newPluginDefinition()
        addPluginProperty(pluginDefinition)
        newPluginConfiguration(pluginDefinition)

        val exception = assertThrows(PluginPropertyRequiredException::class.java) {
            pluginService
                .createPluginConfiguration(
                    "title", ObjectMapper().readTree("{}"), "key"
                )
        }
        assertEquals("Plugin property with name 'name' is required for plugin 'Test Plugin'", exception.message)
    }

    @Test
    fun `should throw exception when required plugin property field is null`() {
        val pluginDefinition = newPluginDefinition()
        addPluginProperty(pluginDefinition)
        newPluginConfiguration(pluginDefinition)

        val exception = assertThrows(PluginPropertyRequiredException::class.java) {
            pluginService
                .createPluginConfiguration(
                    "title", ObjectMapper().readTree("{\"name\": null}"), "key"
                )
        }
        assertEquals("Plugin property with name 'name' is required for plugin 'Test Plugin'", exception.message)
    }

    @Test
    fun `should throw exception when plugin property field has incorrect type`() {
        val pluginDefinition = newPluginDefinition()
        addPluginProperty(pluginDefinition)
        newPluginConfiguration(pluginDefinition)

        val exception = assertThrows(PluginPropertyParseException::class.java) {
            pluginService
                .createPluginConfiguration(
                    "title", ObjectMapper().readTree("{\"name\": [\"incorrect-type\"]}"), "key"
                )
        }
        assertEquals("Plugin property with name 'name' failed to parse for plugin 'Test Plugin'", exception.message)
    }

    @Test
    fun `should get plugin action definitions from repository by key`(){
        whenever(pluginActionDefinitionRepository.findByIdPluginDefinitionKey("test")).thenReturn(
            listOf(
                PluginActionDefinition(
                    PluginActionDefinitionId(
                        "some-key",
                        mock()
                    ),
                    "title",
                    "description",
                    "method",
                    listOf(ActivityType.USER_TASK)
                )
            )
        )

        val actions = pluginService.getPluginDefinitionActions("test", null)

        verify(pluginActionDefinitionRepository).findByIdPluginDefinitionKey("test")

        assertEquals(1, actions.size)
        assertEquals("some-key", actions[0].key)
        assertEquals("title", actions[0].title)
        assertEquals("description", actions[0].description)
    }

    @Test
    fun `should get plugin action definitions from repository by key and activityType`(){
        whenever(pluginActionDefinitionRepository.findByIdPluginDefinitionKeyAndActivityTypes("test", ActivityType.USER_TASK)).thenReturn(
            listOf(
                PluginActionDefinition(
                    PluginActionDefinitionId(
                        "some-key",
                        mock()
                    ),
                    "title",
                    "description",
                    "method",
                    listOf(ActivityType.USER_TASK)
                )
            )
        )

        val actions = pluginService.getPluginDefinitionActions("test", ActivityType.USER_TASK)

        verify(pluginActionDefinitionRepository).findByIdPluginDefinitionKeyAndActivityTypes("test",
            ActivityType.USER_TASK)

        assertEquals(1, actions.size)
        assertEquals("some-key", actions[0].key)
        assertEquals("title", actions[0].title)
        assertEquals("description", actions[0].description)
    }

    private fun newPluginDefinition(): PluginDefinition {
        val pluginDefinition = PluginDefinition(
            "TestPlugin",
            "Test Plugin",
            "description",
            "className",
            mutableSetOf()
        )
        whenever(pluginDefinitionRepository.getById("key")).thenReturn(pluginDefinition)
        return pluginDefinition
    }

    private fun addPluginProperty(pluginDefinition: PluginDefinition) {
        (pluginDefinition.pluginProperties as MutableSet).add(
            PluginProperty(
                PluginPropertyId("property1", pluginDefinition),
                "property1",
                true,
                "name",
                String::class.java.name
            )
        )
    }

    private fun newPluginConfiguration(pluginDefinition: PluginDefinition): PluginConfiguration {
        val pluginConfiguration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            ObjectMapper().readTree("{\"name\": \"whatever\" }"),
            pluginDefinition
        )
        whenever(pluginConfigurationRepository.save(any())).thenReturn(pluginConfiguration)
        return pluginConfiguration
    }
}
