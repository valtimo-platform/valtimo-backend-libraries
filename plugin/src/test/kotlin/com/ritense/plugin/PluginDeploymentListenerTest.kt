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

package com.ritense.plugin

import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginCategory
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.exception.PluginDefinitionNotDeployedException
import com.ritense.plugin.repository.PluginActionDefinitionRepository
import com.ritense.plugin.repository.PluginActionPropertyDefinitionRepository
import com.ritense.plugin.repository.PluginCategoryRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertFailsWith

internal class PluginDeploymentListenerTest {

    lateinit var pluginDefinitionResolver: PluginDefinitionResolver
    lateinit var pluginCategoryResolver: PluginCategoryResolver
    lateinit var pluginDefinitionRepository: PluginDefinitionRepository
    lateinit var pluginCategoryRepository: PluginCategoryRepository
    lateinit var pluginActionDefinitionRepository: PluginActionDefinitionRepository
    lateinit var pluginActionPropertyDefinitionRepository: PluginActionPropertyDefinitionRepository
    lateinit var pluginDeploymentListener: PluginDeploymentListener

    @BeforeEach
    fun beforeEach() {
        pluginDefinitionResolver = mock()
        pluginCategoryResolver = mock()
        pluginDefinitionRepository = mock()
        pluginCategoryRepository = mock()
        pluginActionDefinitionRepository = mock()
        pluginActionPropertyDefinitionRepository = mock()
        pluginDeploymentListener = PluginDeploymentListener(
            pluginDefinitionResolver,
            pluginCategoryResolver,
            pluginDefinitionRepository,
            pluginCategoryRepository,
            pluginActionDefinitionRepository,
            pluginActionPropertyDefinitionRepository
        )
    }

    @Test
    fun `should deploy plugin`() {
        val pluginDefinitionCaptor = argumentCaptor<PluginDefinition>()
        val pluginsToDeploy = mapOf<Class<*>, Plugin>(
            UnitTestPlugin::class.java to Plugin(
                "key",
                "title",
                "description"
            )
        )

        whenever(pluginDefinitionResolver.findPluginClasses()).thenReturn(pluginsToDeploy)
        whenever(pluginDefinitionRepository.save(any())).thenAnswer { it.arguments[0] }

        pluginDeploymentListener.deployPlugins()

        verify(pluginDefinitionResolver).findPluginClasses()
        verify(pluginDefinitionRepository).save(pluginDefinitionCaptor.capture())
        val capturedPluginDefinition = pluginDefinitionCaptor.firstValue
        assertEquals("key", capturedPluginDefinition.key)
        assertEquals("title", capturedPluginDefinition.title)
        assertEquals("description", capturedPluginDefinition.description)
        assertEquals("com.ritense.plugin.PluginDeploymentListenerTest\$UnitTestPlugin",
            capturedPluginDefinition.fullyQualifiedClassName)
    }

    @Test
    fun `should throw PluginDefinitionNotDeployedException on any exception`() {
        val pluginsToDeploy = mapOf<Class<*>, Plugin>(
            TestPlugin::class.java to Plugin(
                "key",
                "title",
                "description"
            )
        )

        whenever(pluginDefinitionResolver.findPluginClasses()).thenReturn(pluginsToDeploy)
        whenever(pluginDefinitionRepository.save(any())).doThrow(RuntimeException("Some exception"))

        val exception = assertFailsWith<PluginDefinitionNotDeployedException> {
            pluginDeploymentListener.deployPlugins()
        }

        assertEquals("key", exception.pluginKey)
        assertEquals("com.ritense.plugin.TestPlugin", exception.fullyQualifiedClassName)
        assertEquals("Unable to deploy plugin with key 'key' and class name 'com.ritense.plugin.TestPlugin'",
            exception.message)
    }

    @Test
    fun `should deploy plugin category`() {
        val pluginCategoryCaptor = argumentCaptor<com.ritense.plugin.domain.PluginCategory>()
        val pluginsToDeploy = mapOf<Class<*>, Plugin>()
        val pluginCategoriesToDeploy = mapOf<Class<*>, PluginCategory>(
            String::class.java to PluginCategory(
                "key"
            ),
        )

        whenever(pluginCategoryResolver.findPluginCategoryClasses()).thenReturn(pluginCategoriesToDeploy)
        whenever(pluginDefinitionResolver.findPluginClasses()).thenReturn(pluginsToDeploy)
        whenever(pluginDefinitionRepository.save(any())).thenAnswer { it.arguments[0] }

        pluginDeploymentListener.deployPlugins()

        verify(pluginCategoryResolver).findPluginCategoryClasses()
        verify(pluginCategoryRepository).save(pluginCategoryCaptor.capture())
        val capturedPluginCategory = pluginCategoryCaptor.firstValue
        assertEquals("key", capturedPluginCategory.key)
        assertEquals("java.lang.String",
            capturedPluginCategory.fullyQualifiedClassName)
    }

    @Test
    fun `should throw exception on duplicate plugin`() {
        val pluginCategoriesToDeploy = mapOf<Class<*>, PluginCategory>(
            Char::class.java to PluginCategory(
                "key"
            ),
            String::class.java to PluginCategory(
                "key"
            ),
            Int::class.java to PluginCategory(
                "other-key"
            ),
            Double::class.java to PluginCategory(
                "other-key"
            )
        )

        whenever(pluginCategoryResolver.findPluginCategoryClasses()).thenReturn(pluginCategoriesToDeploy)

        val exception = assertFailsWith<IllegalStateException> {
            pluginDeploymentListener.deployPlugins()
        }

        assertEquals(IllegalStateException::class.java, exception::class.java)
        val expectedMessage = """
            Found duplicate plugin categories:
             - category 'key' for classes [char, java.lang.String]
             - category 'other-key' for classes [int, double]
        """.trimIndent()
        assertEquals(expectedMessage, exception.message)
    }

    private class UnitTestPlugin()
}
