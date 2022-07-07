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

package com.ritense.plugin

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProperty
import com.ritense.plugin.domain.PluginPropertyId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PluginFactoryTest {
    lateinit var pluginFactory: PluginFactory<*>

    @BeforeEach
    fun init() {
        pluginFactory = TestPluginFactory("someObject")
    }

    @Test
    fun `should be able to create plugin instance`() {
        val pluginDefinition = PluginDefinition(
            "key",
            "title",
            "description",
            TestPlugin::class.java.name)
        val pluginConfiguration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            ObjectMapper().valueToTree("{\"name\": \"whatever\" }"),
            pluginDefinition
        )

        assertTrue(pluginFactory.canCreate(pluginConfiguration))
    }

    @Test
    fun `should not be able to create plugin instance for different plugin definition`() {
        val pluginDefinition = PluginDefinition("key", "title", "description", "className")
        val pluginConfiguration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            ObjectMapper().valueToTree("{\"name\": \"whatever\" }"),
            pluginDefinition
        )

        assertFalse(pluginFactory.canCreate(pluginConfiguration))
    }

    @Test
    fun `should create and initialize plugin instance`() {
        val properties: MutableSet<PluginProperty> = mutableSetOf()
        val pluginDefinition = PluginDefinition(
            "key",
            "title",
            "description",
            TestPlugin::class.java.name,
            properties
        )

        properties.add(
            PluginProperty(
                PluginPropertyId(
                    "property1",
                    pluginDefinition
                ),
                "property1",
                true,
                "property1",
                String::class.java.name
            )
        )

        properties.add(
            PluginProperty(
                PluginPropertyId(
                    "property2",
                    pluginDefinition
                ),
                "property2",
                true,
                "property2",
                Boolean::class.java.name
            )
        )

        properties.add(
            PluginProperty(
                PluginPropertyId(
                    "property3",
                    pluginDefinition
                ),
                "property3",
                true,
                "property3",
                Number::class.java.name
            )
        )

        val pluginConfiguration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            ObjectMapper().valueToTree(
                    TestPluginConfiguredProperties(property1 = "whatever", property3 = 2)
            ),
            pluginDefinition
        )

        val pluginInstance = pluginFactory.create(pluginConfiguration) as TestPlugin
        assertEquals("whatever", pluginInstance.property1)
        assertNull(pluginInstance.property2)
        assertEquals(2, pluginInstance.property3)
    }
}