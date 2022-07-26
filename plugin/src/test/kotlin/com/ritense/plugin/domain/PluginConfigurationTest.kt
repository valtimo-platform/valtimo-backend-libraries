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

package com.ritense.plugin.domain

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.valtimo.contract.json.Mapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PluginConfigurationTest {

    lateinit var configuration: PluginConfiguration

    @BeforeEach
    fun init() {
        val input = """
            {
                "property1": "old-value",
                "property2": false,
                "property3": 123
            }
        """.trimMargin()

        val props = mutableSetOf<PluginProperty>()

        val pluginDefinition = PluginDefinition(
            "key",
            "title",
            "description",
            "some-class",
            props
        )

        props.add(
            PluginProperty(
                PluginPropertyId("key", pluginDefinition),
                "title",
                false,
                true,
                "property1",
                "test"
            )
        )

        props.add(
            PluginProperty(
                PluginPropertyId("key", pluginDefinition),
                "title",
                false,
                false,
                "property2",
                "test"
            )
        )
        props.add(
            PluginProperty(
                PluginPropertyId("key", pluginDefinition),
                "title",
                false,
                false,
                "property3",
                "test"
            )
        )

        configuration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            Mapper.INSTANCE.get().readTree(input) as ObjectNode,
            pluginDefinition
        )
    }

    @Test
    fun `should update property when new value is present`() {
        val input = """
            {
                "property1": "test123",
                "property2": false,
                "property3": 456
            }
        """.trimMargin()

        configuration.updateProperties(Mapper.INSTANCE.get().readTree(input) as ObjectNode)

        assertEquals(456, configuration.properties?.get("property3")?.intValue())
    }

    @Test
    fun `should update secret property when new value is not null`() {
        val input = """
            {
                "property1": "test",
                "property2": false,
                "property3": 123
            }
        """.trimMargin()

        configuration.updateProperties(Mapper.INSTANCE.get().readTree(input) as ObjectNode)

        assertEquals("test", configuration.properties?.get("property1")?.textValue())
    }

    @Test
    fun `should not update secret property when new value is null`() {
        val input = """
            {
                "property1": null,
                "property2": false,
                "property3": 123
            }
        """.trimMargin()

        configuration.updateProperties(Mapper.INSTANCE.get().readTree(input) as ObjectNode)

        assertEquals("old-value", configuration.properties?.get("property1")?.textValue())
    }

    @Test
    fun `should not update secret property when new value is missing`() {
        val input = """
            {
                "property2": false,
                "property3": 123
            }
        """.trimMargin()

        configuration.updateProperties(Mapper.INSTANCE.get().readTree(input) as ObjectNode)

        assertEquals("old-value", configuration.properties?.get("property1")?.textValue())
    }

    @Test
    fun `should not update secret property when new value is empty string`() {
        val input = """
            {
                "property1": "",
                "property2": false,
                "property3": 123
            }
        """.trimMargin()

        configuration.updateProperties(Mapper.INSTANCE.get().readTree(input) as ObjectNode)

        assertEquals("old-value", configuration.properties?.get("property1")?.textValue())
    }


    @Test
    fun `should update property when new value is null`() {
        val input = """
            {
                "property1": "test123",
                "property2": false,
                "property3": null
            }
        """.trimMargin()

        configuration.updateProperties(Mapper.INSTANCE.get().readTree(input) as ObjectNode)

        assertTrue(configuration.properties?.get("property3")!!.isNull)
    }

}