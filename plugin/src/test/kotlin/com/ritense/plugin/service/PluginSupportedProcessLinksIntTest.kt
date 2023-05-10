/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ritense.plugin.service

import com.ritense.plugin.BaseIntegrationTest
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Transactional
class PluginSupportedProcessLinksIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var pluginSupportedProcessLinks: PluginSupportedProcessLinksHandler

    @Autowired
    lateinit var pluginConfigurationRepository: PluginConfigurationRepository

    @Autowired
    lateinit var pluginDefinitionRepository: PluginDefinitionRepository

    lateinit var categoryPluginConfiguration: PluginConfiguration

    @BeforeEach
    fun init() {

        val categoryPluginDefinition = pluginDefinitionRepository.getById("test-category-plugin")
        categoryPluginConfiguration = pluginConfigurationRepository.save(
            PluginConfiguration(
                PluginConfigurationId.newId(),
                "title",
                null,
                categoryPluginDefinition
            )
        )
    }

    @Test
    fun `should return a plugin process link type for ServiceTaskStart with enabled true`() {
        val result = pluginSupportedProcessLinks.getProcessLinkType("bpmn:ServiceTask:start")
        assertEquals("plugin", result?.processLinkType)
        assertEquals(true, result?.enabled)
    }

    @Test
    fun `should return a plugin process link type for UserTaskCreate with enabled false`() {
        val result = pluginSupportedProcessLinks.getProcessLinkType("bpmn:UserTask:create")
        assertEquals("plugin", result?.processLinkType)
        assertEquals(false, result?.enabled)
    }

    @Test
    fun `should not return a plugin process link type for StartEventStart`() {
        val result = pluginSupportedProcessLinks.getProcessLinkType("bpmn:StartEvent:start")
        assertNull(result)
    }

}