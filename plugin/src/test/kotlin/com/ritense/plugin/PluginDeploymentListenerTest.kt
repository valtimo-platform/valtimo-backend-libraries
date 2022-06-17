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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.repository.PluginDefinitionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PluginDeploymentListenerTest {

    lateinit var pluginDefinitionResolver: PluginDefinitionResolver
    lateinit var pluginDefinitionRepository: PluginDefinitionRepository
    lateinit var pluginDeploymentListener: PluginDeploymentListener

    @BeforeEach
    fun beforeAll() {
        pluginDefinitionResolver = mock()
        pluginDefinitionRepository = mock()
        pluginDeploymentListener = PluginDeploymentListener(
            pluginDefinitionResolver,
            pluginDefinitionRepository
        )
    }

    @Test
    fun `should deploy plugin`() {
        val pluginDefinitionCaptor = argumentCaptor<PluginDefinition>()
        val pluginsToDeploy = mapOf<Class<*>, Plugin>(
            TestPlugin::class.java to Plugin(
                "key",
                "title",
                "description"
            )
        )

        whenever(pluginDefinitionResolver.findPluginClasses()).thenReturn(pluginsToDeploy)
        whenever(pluginDefinitionRepository.save(any())).thenAnswer { it.arguments[0] }

        pluginDeploymentListener.deployPluginDefinitions()

        verify(pluginDefinitionResolver).findPluginClasses()
        verify(pluginDefinitionRepository).save(pluginDefinitionCaptor.capture())
        val capturedPluginDefinition = pluginDefinitionCaptor.firstValue
        assertEquals("key", capturedPluginDefinition.key)
        assertEquals("title", capturedPluginDefinition.title)
        assertEquals("description", capturedPluginDefinition.description)
        assertEquals("com.ritense.plugin.TestPlugin", capturedPluginDefinition.fullyQualifiedClassName)
    }

}