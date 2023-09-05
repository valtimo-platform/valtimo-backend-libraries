package com.ritense.plugin.web.rest.result

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProperty
import com.ritense.valtimo.contract.json.Mapper
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Test

internal class PluginConfigurationDtoTest {

    @Test
    fun `should create dto without properties`() {
        val pluginDefinition = PluginDefinition(
            "key",
            "title",
            "description",
            "some-class"
        )

        val configuration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            null,
            pluginDefinition
        )


        val configurationDto = PluginConfigurationDto(configuration)

        assertEquals(configuration.id.id.toString(), configurationDto.id)
        assertEquals(configuration.title, configurationDto.title)
        assertEquals(configuration.pluginDefinition, configurationDto.pluginDefinition)
        assertNull(configurationDto.properties)

    }

    @Test
    fun `should filter out secret properties`() {
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
                "key",
                pluginDefinition,
                "title",
                false,
                true,
                "property1",
                "test"
            )
        )

        props.add(
            PluginProperty(
                "key",
                pluginDefinition,
                "title",
                false,
                false,
                "property2",
                "test"
            )
        )
        props.add(
            PluginProperty(
                "key",
                pluginDefinition,
                "title",
                false,
                false,
                "property3",
                "test"
            )
        )

        val configuration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            Mapper.INSTANCE.get().readTree(input) as ObjectNode,
            pluginDefinition
        )

        val configurationDto = PluginConfigurationDto(configuration)

        assertEquals(configuration.id.id.toString(), configurationDto.id)
        assertEquals(configuration.title, configurationDto.title)
        assertEquals(configuration.pluginDefinition, configurationDto.pluginDefinition)
        assertEquals(2, configurationDto.properties!!.size())

    }

}