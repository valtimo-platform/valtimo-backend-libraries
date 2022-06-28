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

import com.ritense.plugin.domain.ActivityType
import com.ritense.plugin.domain.ActivityType.SERVICE_TASK
import com.ritense.plugin.domain.ActivityType.USER_TASK
import com.ritense.plugin.domain.PluginActionDefinition
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.repository.PluginActionDefinitionRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasProperty
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.hamcrest.core.IsIterableContaining.hasItems
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import javax.transaction.Transactional

internal class PluginDeploymentListenerIT: BaseIntegrationTest() {

    @Autowired
    lateinit var pluginDefinitionRepository: PluginDefinitionRepository

    @Autowired
    lateinit var pluginActionDefinitionRepository: PluginActionDefinitionRepository

    @Test
    @Transactional
    fun `should deploy test plugin`() {
        val deployedPlugins = pluginDefinitionRepository.findAll()
        val deployedActions = pluginActionDefinitionRepository.findAll()

        assertEquals(1, deployedPlugins.size)
        assertEquals("test-plugin", deployedPlugins[0].key)
        assertEquals("Test plugin", deployedPlugins[0].title)
        assertEquals("This is a test plugin used to verify plugin framework functionality",
            deployedPlugins[0].description)
        assertEquals("com.ritense.plugin.TestPlugin", deployedPlugins[0].fullyQualifiedClassName)

        assertTestActionPresent(deployedActions)
        assertOtherTestActionPresent(deployedActions)
        assertInheritedActionPresent(deployedActions)
        assertOverridingActionPresent(deployedActions)
        assertOverriddenActionNotPresent(deployedActions)
    }

    private fun assertTestActionPresent(deployedActions: List<PluginActionDefinition>) {
        assertActionDeployed(
            deployedActions,
            "test-action",
            "test-plugin",
            "Test action",
            "This is an action used to verify plugin framework functionality",
            "testAction",
            arrayOf(USER_TASK)
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
            arrayOf(USER_TASK, SERVICE_TASK)
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
            arrayOf(USER_TASK, SERVICE_TASK)
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
            arrayOf(SERVICE_TASK)
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
        activityTypes: Array<ActivityType>
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