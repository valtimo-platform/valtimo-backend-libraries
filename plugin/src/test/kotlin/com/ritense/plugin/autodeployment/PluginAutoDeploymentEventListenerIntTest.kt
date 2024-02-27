/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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
import com.ritense.plugin.service.PluginConfigurationSearchParameters
import com.ritense.plugin.service.PluginService
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PluginAutoDeploymentEventListenerIntTest: BaseIntegrationTest() {

    @Autowired
    lateinit var pluginService : PluginService

    @Test
    fun `should deploy plugin configuration`(){
        val result = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(pluginConfigurationTitle = "auto deployment test plugin")
        )
        assertTrue { result.size == 1 }
        assertEquals("https://www.google.com", result[0].properties?.get("property1")?.textValue())
        assertTrue (result[0].properties?.get("property2")?.booleanValue()!!)
        assertEquals(3, result[0].properties?.get("property3")?.intValue())
    }

    @Test
    fun `should deploy plugin configuration with null property`(){
        val result = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(pluginConfigurationTitle = "auto deployment test plugin with null property")
        )
        assertNull(result[0].properties!!.get("property1")!!.textValue())
    }


}