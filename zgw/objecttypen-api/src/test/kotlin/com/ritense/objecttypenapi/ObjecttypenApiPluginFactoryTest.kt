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

package com.ritense.objecttypenapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProperty
import com.ritense.plugin.service.PluginService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class ObjecttypenApiPluginFactoryTest {

    @Test
    fun `should create ObjecttypenApiPlugin`() {
        val pluginService: PluginService = mock()
        val authenticationMock = mock<ObjecttypenApiAuthentication>()
        whenever(pluginService.createInstance(any())).thenReturn(authenticationMock)

        val factory = ObjecttypenApiPluginFactory(pluginService)

        val objecttypenApiPluginProperties: String = """
            {
              "url": "http://objecttypen.plugin.url",
              "authenticationPluginConfiguration": "7c4e15e4-c245-4fd9-864b-dd36baa02abf"
            }
        """.trimIndent()

        val pluginDefinition = createPluginDefinition()
        val pluginConfiguration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            ObjectMapper().readTree(objecttypenApiPluginProperties) as ObjectNode,
            pluginDefinition
        )
        val plugin = factory.create(pluginConfiguration)

        Assertions.assertEquals("http://objecttypen.plugin.url", plugin.url)
        Assertions.assertEquals(authenticationMock, plugin.authenticationPluginConfiguration)
    }

    private fun createPluginDefinition(): PluginDefinition {
        val propertyDefinitions = mutableSetOf<PluginProperty>()
        val pluginDefinition = PluginDefinition(
            "key",
            "title",
            "description",
            "class",
            propertyDefinitions
        )

        propertyDefinitions.add(
            PluginProperty("url", pluginDefinition, "title", required = true,
                secret = false, "url","java.lang.String")
        )
        propertyDefinitions.add(
            PluginProperty("authenticationPluginConfiguration", pluginDefinition, "title",
                required = true, secret = false, "authenticationPluginConfiguration",
                "com.ritense.objecttypenapi.ObjecttypenApiAuthentication")
        )

        return pluginDefinition
    }
}