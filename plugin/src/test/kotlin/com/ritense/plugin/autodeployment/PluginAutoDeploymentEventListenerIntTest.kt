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

package com.ritense.plugin.autodeployment

import com.ritense.plugin.BaseIntegrationTest
import com.ritense.plugin.repository.PluginDefinitionRepository
import com.ritense.plugin.service.PluginConfigurationSearchParameters
import com.ritense.plugin.service.PluginService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PluginAutoDeploymentEventListenerIntTest: BaseIntegrationTest() {

    @Autowired
    lateinit var pluginService : PluginService

    @Autowired
    lateinit var pluginDefinitionRepository: PluginDefinitionRepository

    @Test
    fun `should deploy plugin configuration`(){
        val result = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(pluginDefinitionKey = "auto-deployment-test-plugin")
        )
        assertTrue { result.size == 1 }
        assertEquals("property one", result[0].properties?.get("property1")?.textValue())
        assertTrue (result[0].properties?.get("property2")?.booleanValue()!!)
        assertEquals(3, result[0].properties?.get("property3")?.intValue())
    }


}