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

package com.ritense.valtimo.mapper

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.plugin.service.PluginService.Companion.PROCESS_LINK_TYPE_PLUGIN
import com.ritense.processlink.domain.ActivityTypeWithEventName.SERVICE_TASK_START
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class PluginProcessLinkAutodeploymentIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var processLinkService: ProcessLinkService

    @Test
    fun `should deploy plugin process link on startup`() {
        val processLinks = processLinkService.getProcessLinks(
            "TestTask",
            SERVICE_TASK_START,
            PROCESS_LINK_TYPE_PLUGIN
        )

        assertEquals(1, processLinks.size)
        assertTrue(processLinks[0] is PluginProcessLink)
        val pluginProcessLink = processLinks[0] as PluginProcessLink
        assertEquals("TestTask", pluginProcessLink.activityId)
        assertEquals(SERVICE_TASK_START, pluginProcessLink.activityType)
        assertEquals(PROCESS_LINK_TYPE_PLUGIN, pluginProcessLink.processLinkType)
        assertEquals("0a750334-a065-48fa-bb02-293d21df2213", pluginProcessLink.pluginConfigurationId.id.toString())
        assertEquals("test-action", pluginProcessLink.pluginActionDefinitionKey)
        assertEquals("""{"testActionProperty":"test-value"}""", jacksonObjectMapper().writeValueAsString(pluginProcessLink.actionProperties))
    }
}