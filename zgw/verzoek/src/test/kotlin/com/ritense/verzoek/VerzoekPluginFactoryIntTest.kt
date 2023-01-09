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

package com.ritense.verzoek

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.BaseIntegrationTest
import com.ritense.plugin.PluginDeploymentListener
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.repository.PluginDefinitionRepository
import com.ritense.plugin.service.PluginService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import java.net.URI
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.isAccessible

internal class VerzoekPluginFactoryIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var verzoekPluginFactory: VerzoekPluginFactory

    @Autowired
    lateinit var pluginService: PluginService

    @Test
    fun `should create VerzoekPlugin`() {
        val verzoekPluginProperties: String = """
            {
              "notificatiesApiPluginConfiguration": {
                "url": "https://example.com/my-notificatie-api-url",
                "authenticationPluginConfiguration": {
                  "clientId": "my-client-id",
                  "clientSecret": "my-client-secret"
                }
              },
              "objectManagementId": "0b993a22-aa70-49a8-934a-79b17a70df6f",
              "systemProcessDefinitionKey": "verzoek-process",
              "rsin": "637549971",
              "verzoekProperties": [{
                "type": "objection",
                "caseDefinitionName": "objection-case-definition",
                "processDefinitionKey": "objection-process",
                "initiatorRoltypeUrl": "https://example.com/my-role-type",
                "initiatorRolDescription": "Initiator"
              }]
            }
        """.trimIndent()
        val pluginConfiguration = pluginService.createPluginConfiguration(
            "my-verzoek-plugin-configuration",
            jacksonObjectMapper().readTree(verzoekPluginProperties).deepCopy(),
            "verzoek"
        )

        val plugin = verzoekPluginFactory.create(pluginConfiguration)

        assertEquals(URI("https://example.com/my-notificatie-api-url"), plugin.notificatiesApiPluginConfiguration.url)
        assertNotNull(plugin.notificatiesApiPluginConfiguration.authenticationPluginConfiguration)
        assertEquals("verzoek-process", plugin.systemProcessDefinitionKey)
        assertEquals("637549971", plugin.rsin.toString())
        assertEquals(1, plugin.verzoekProperties.size)
        assertEquals("objection", plugin.verzoekProperties[0].type)
        assertEquals("objection-case-definition", plugin.verzoekProperties[0].caseDefinitionName)
        assertEquals("objection-process", plugin.verzoekProperties[0].processDefinitionKey)
        assertEquals("https://example.com/my-role-type", plugin.verzoekProperties[0].initiatorRoltypeUrl.toString())
        assertEquals("Initiator", plugin.verzoekProperties[0].initiatorRolDescription)
    }

    private fun createPluginDefinition(): PluginDefinition {
        val pluginDefinitionRepositoryMock = mock<PluginDefinitionRepository>()
        whenever(pluginDefinitionRepositoryMock.save(any())).thenAnswer { it.arguments[0] }
        val pluginDeploymentListener = PluginDeploymentListener(
            mock(),
            mock(),
            pluginDefinitionRepositoryMock,
            mock(),
            mock(),
            mock()
        )
        val createPluginDefinitionFunction = PluginDeploymentListener::class.functions
            .find { it.name == "createPluginDefinition" }!!
        createPluginDefinitionFunction.isAccessible = true
        return createPluginDefinitionFunction.call(
            pluginDeploymentListener,
            VerzoekPlugin::class.java,
            VerzoekPlugin::class.findAnnotations(Plugin::class).single()
        ) as PluginDefinition
    }
}
