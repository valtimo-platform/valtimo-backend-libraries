/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

import com.ritense.plugin.domain.PluginActionDefinition
import com.ritense.plugin.domain.PluginCategory
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProperty
import com.ritense.plugin.repository.PluginActionDefinitionRepository
import com.ritense.plugin.repository.PluginActionPropertyDefinitionRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ActivityTypeWithEventName.SERVICE_TASK_START
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.hasProperty
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.hamcrest.core.IsIterableContaining.hasItems
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

internal class PluginDeploymentListenerIT: BaseIntegrationTest() {

    @Autowired
    lateinit var pluginDefinitionRepository: PluginDefinitionRepository

    @Autowired
    lateinit var pluginActionDefinitionRepository: PluginActionDefinitionRepository

    @Autowired
    lateinit var pluginActionPropertyDefinitionRepository: PluginActionPropertyDefinitionRepository

    @Test
    @Transactional
    fun `should deploy test plugin`() {
        val deployedPlugins = pluginDefinitionRepository.findAll()
        val deployedActions = pluginActionDefinitionRepository.findAll()

        val plugin =
            deployedPlugins.find { it.key == "test-plugin" } ?: fail { "test-plugin was not deployed!" }
        assertEquals("Test plugin", plugin.title)
        assertEquals("This is a test plugin used to verify plugin framework functionality",
            plugin.description)
        assertEquals("com.ritense.plugin.TestPlugin", plugin.fullyQualifiedClassName)

        assertPluginPropertiesPresent(plugin.properties.toList(), plugin.key)
        assertTestActionPresent(deployedActions)
        assertOtherTestActionPresent(deployedActions)
        assertInheritedActionPresent(deployedActions)
        assertOverridingActionPresent(deployedActions)
        assertOverriddenActionNotPresent(deployedActions)
        assertPluginCategoryPresent(plugin.categories)

        val deployedActionProperties = pluginActionPropertyDefinitionRepository.findAll()
        assertThat(deployedActionProperties.size, `is`(2))
    }

    private fun assertPluginPropertiesPresent(
        pluginProperties: List<PluginProperty>,
        definitionKey: String
    ) {
        assertThat(pluginProperties.size, greaterThanOrEqualTo(3))
        assertThat(
            pluginProperties,
            hasItems(
                allOf(
                    hasProperty("id",
                        allOf(
                            hasProperty("key", `is`("property1")),
                            hasProperty<String>("pluginDefinitionId", `is`(definitionKey)),
                        ),
                    ),
                    hasProperty("pluginDefinition",
                        hasProperty<String>("key", `is`(definitionKey))
                    ),
                    hasProperty("required", `is`(true)),
                    hasProperty("fieldName", `is`("property1")),
                    hasProperty("fieldType", `is`(String::class.java.name))
                ),
                allOf(
                    hasProperty("id",
                        allOf(
                            hasProperty("key", `is`("property2")),
                            hasProperty<String>("pluginDefinitionId", `is`(definitionKey)),
                        ),
                    ),
                    hasProperty("pluginDefinition",
                        hasProperty<String>("key", `is`(definitionKey))
                    ),
                    hasProperty("required", `is`(false))
                )
            )
        )
    }

    private fun assertPluginCategoryPresent(
        pluginCategories: Set<PluginCategory>
    ) {
        assertEquals(1, pluginCategories.size)
        assertThat(
            pluginCategories,
            hasItems(
                allOf(
                    hasProperty("key", `is`("test-interface")),
                    hasProperty("fullyQualifiedClassName",
                        `is`("com.ritense.plugin.TestPluginInterface"))
                )
            )
        )
    }

    private fun assertTestActionPresent(deployedActions: List<PluginActionDefinition>) {
        assertActionDeployed(
            deployedActions,
            "test-action",
            "test-plugin",
            "Test action",
            "This is an action used to verify plugin framework functionality",
            "testAction",
            arrayOf(SERVICE_TASK_START)
        )
    }

    private fun assertOtherTestActionPresent(deployedActions: List<PluginActionDefinition>) {
        assertActionDeployed(
            deployedActions,
            "other-test-action",
            "test-plugin",
            "Test action 2",
            "This is an action used to test method overloading",
            "testAction",
            arrayOf(SERVICE_TASK_START)
        )
    }

    private fun assertInheritedActionPresent(deployedActions: List<PluginActionDefinition>) {
        assertActionDeployed(
            deployedActions,
            "parent-test-action",
            "test-plugin",
            "Parent test action",
            "This is an action used to test method inheritance",
            "testAction",
            arrayOf(SERVICE_TASK_START)
        )
    }

    private fun assertOverridingActionPresent(deployedActions: List<PluginActionDefinition>) {
        assertActionDeployed(
            deployedActions,
            "child-override-test-action",
            "test-plugin",
            "Override test action",
            "This is an action used to test method inheritance",
            "overrideAction",
            arrayOf(SERVICE_TASK_START)
        )
    }

    private fun assertOverriddenActionNotPresent(deployedActions: List<PluginActionDefinition>) {
        assertThat(
            deployedActions,
            not(
                hasItems(
                    hasProperty("id",
                        hasProperty<String>("key", `is`("parent-override-test-action")),
                    ),
                )
            )
        )
    }

    private fun assertActionDeployed(
        deployedActions: List<PluginActionDefinition>,
        key: String,
        definitionKey: String,
        title: String,
        description: String,
        methodName: String,
        activityTypes: Array<ActivityTypeWithEventName>
    ) {
        assertThat(
            deployedActions,
            hasItems(
                hasProperty("id",
                    allOf(
                        hasProperty("key", `is`(key)),
                        hasProperty<PluginDefinition>("pluginDefinition",
                            hasProperty<String>("key", `is`(definitionKey))
                        )
                    ),
                ),
                hasProperty("title", `is`(title)),
                hasProperty("description", `is`(description)),
                hasProperty("methodName", `is`(methodName)),
                hasProperty("activityTypes", containsInAnyOrder(*activityTypes))
            )
        )
    }
}
