/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProperty
import com.ritense.valtimo.contract.json.Mapper
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class PluginConfigurationEncryptionTest {

    lateinit var listener: PluginConfigurationEntityListener

    lateinit var encryptionService: EncryptionService

    lateinit var pluginDefinition: PluginDefinition

    @BeforeEach
    fun init() {
        encryptionService = mock()
        whenever(encryptionService.encrypt(any())).thenReturn("output")
        whenever(encryptionService.decrypt(any())).thenReturn("\"output\"")

        listener = PluginConfigurationEntityListener(
            encryptionService,
            ObjectMapper()
        )

        val props = mutableSetOf<PluginProperty>()

        pluginDefinition = PluginDefinition(
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
    }


    @Test
    fun `should only replace secret properties when encrypting`() {

        val input = """
            {
                "property1": "test1",
                "property2": "test2"
            }
        """.trimMargin()

        val configuration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            Mapper.INSTANCE.get().readTree(input) as ObjectNode,
            pluginDefinition
        )

        listener.encryptPropertiesOnSave(configuration)

        assertEquals("output", configuration.rawProperties?.get("property1")?.textValue())
        assertEquals("test2", configuration.rawProperties?.get("property2")?.textValue())
    }

    @Test
    fun `should not replace null values for secret properties when encrypting`() {

        val input = """
            {
                "property1": null,
                "property2": "test2"
            }
        """.trimMargin()

        val configuration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            Mapper.INSTANCE.get().readTree(input) as ObjectNode,
            pluginDefinition
        )

        listener.encryptPropertiesOnSave(configuration)

        assertTrue(configuration.rawProperties?.get("property1")!!.isNull)
        assertEquals("test2", configuration.rawProperties?.get("property2")?.textValue())
    }

    @Test
    fun `should not replace missing values for secret properties when encrypting`() {

        val input = """
            {
                "property2": "test2"
            }
        """.trimMargin()

        val configuration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            Mapper.INSTANCE.get().readTree(input) as ObjectNode,
            pluginDefinition
        )

        listener.encryptPropertiesOnSave(configuration)

        assertNull(configuration.rawProperties?.get("property1"))
        assertEquals("test2", configuration.rawProperties?.get("property2")?.textValue())
    }

    @Test
    fun `should only replace secret properties when decrypting`() {

        val input = """
            {
                "property1": "test1",
                "property2": "test2"
            }
        """.trimMargin()

        val configuration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            Mapper.INSTANCE.get().readTree(input) as ObjectNode,
            pluginDefinition
        )

        listener.setBeans(configuration)
        configuration.decryptProperties()

        assertEquals("output", configuration.properties?.get("property1")?.textValue())
        assertEquals("test2", configuration.properties?.get("property2")?.textValue())
    }

    @Test
    fun `should not replace null values for secret properties when decrypting`() {

        val input = """
            {
                "property1": null,
                "property2": "test2"
            }
        """.trimMargin()

        val configuration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            Mapper.INSTANCE.get().readTree(input) as ObjectNode,
            pluginDefinition
        )

        listener.setBeans(configuration)
        configuration.decryptProperties()

        assertTrue(configuration.properties?.get("property1")!!.isNull)
        assertEquals("test2", configuration.properties?.get("property2")?.textValue())
    }

    @Test
    fun `should not replace missing values for secret properties when decrypting`() {

        val input = """
            {
                "property2": "test2"
            }
        """.trimMargin()

        val configuration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            Mapper.INSTANCE.get().readTree(input) as ObjectNode,
            pluginDefinition
        )

        listener.setBeans(configuration)
        configuration.decryptProperties()

        assertNull(configuration.properties?.get("property1"))
        assertEquals("test2", configuration.properties?.get("property2")?.textValue())
    }

}