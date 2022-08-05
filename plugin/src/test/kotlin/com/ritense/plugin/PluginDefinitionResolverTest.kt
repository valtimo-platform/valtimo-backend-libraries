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

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PluginDefinitionResolverTest {

    @Test
    fun `should find annotated plugin class`() {
        val resolver = PluginDefinitionResolver()

        val pluginMap = resolver.findPluginClasses()
        assertThat(pluginMap.size, Matchers.greaterThanOrEqualTo(1))
        assertTrue(pluginMap.contains(TestPlugin::class.java))
        val testPlugin = pluginMap[TestPlugin::class.java]!!
        assertEquals("test-plugin", testPlugin.key)
        assertEquals("Test plugin", testPlugin.title)
        assertEquals("This is a test plugin used to verify plugin framework functionality",
            testPlugin.description)
    }

}