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

import com.ritense.plugin.repository.PluginDefinitionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class PluginDeploymentListenerIT: BaseIntegrationTest() {

    @Autowired
    lateinit var pluginDefinitionRepository: PluginDefinitionRepository

    @Test
    fun `should deploy test plugin`() {
        val deployedPlugins = pluginDefinitionRepository.findAll()

        assertEquals(1, deployedPlugins.size)
        assertEquals("test-plugin", deployedPlugins[0].key)
        assertEquals("Test plugin", deployedPlugins[0].title)
        assertEquals("This is a test plugin used to verify plugin framework functionality",
            deployedPlugins[0].description)
        assertEquals("com.ritense.plugin.TestPlugin", deployedPlugins[0].fullyQualifiedClassName)
    }

}