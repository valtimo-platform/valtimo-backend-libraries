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
import com.ritense.notificatiesapiauthentication.NotificatiesApiAuthenticationPlugin
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.service.PluginService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.net.URI

internal class VerzoekPluginFactoryIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var verzoekPluginFactory: VerzoekPluginFactory

    @Autowired
    lateinit var pluginService: PluginService

    lateinit var notificatiesApiPluginConfiguration: PluginConfiguration

    @BeforeEach
    fun beforeEach() {
        val notificatiesApiAuthenticationPluginConfiguration = createPluginConfiguration(
            "notificatiesapiauthentication", """
            {
              "clientId": "my-client-id",
              "clientSecret": "my-client-secret"
            }
        """.trimIndent()
        )

        notificatiesApiPluginConfiguration = createPluginConfiguration(
            "notificatiesapi", """
            {
              "url": "https://example.com/my-notificatie-api-url",
              "authenticationPluginConfiguration": "${notificatiesApiAuthenticationPluginConfiguration.id.id}"
            }
        """.trimIndent()
        )
    }

    @Test
    fun `should create VerzoekPlugin`() {
        val verzoekPluginConfiguration = createPluginConfiguration(
            "verzoek", """
            {
              "notificatiesApiPluginConfiguration": "${notificatiesApiPluginConfiguration.id.id}",
              "objectManagementId": "0b993a22-aa70-49a8-934a-79b17a70df6f",
              "processToStart": "verzoek-process",
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
        )

        val plugin = verzoekPluginFactory.create(verzoekPluginConfiguration)

        assertEquals(URI("https://example.com/my-notificatie-api-url"), plugin.notificatiesApiPluginConfiguration.url)
        assertTrue(plugin.notificatiesApiPluginConfiguration.authenticationPluginConfiguration is NotificatiesApiAuthenticationPlugin)
        assertEquals("verzoek-process", plugin.processToStart)
        assertEquals("637549971", plugin.rsin.toString())
        assertEquals(1, plugin.verzoekProperties.size)
        assertEquals("objection", plugin.verzoekProperties[0].type)
        assertEquals("objection-case-definition", plugin.verzoekProperties[0].caseDefinitionName)
        assertEquals("objection-process", plugin.verzoekProperties[0].processDefinitionKey)
        assertEquals("https://example.com/my-role-type", plugin.verzoekProperties[0].initiatorRoltypeUrl.toString())
        assertEquals("Initiator", plugin.verzoekProperties[0].initiatorRolDescription)
    }

    private fun createPluginConfiguration(pluginDefinitionKey: String, pluginProperties: String): PluginConfiguration {
        return pluginService.createPluginConfiguration(
            "my-configuration-$pluginDefinitionKey-${pluginProperties.hashCode()}",
            jacksonObjectMapper().readTree(pluginProperties).deepCopy(),
            pluginDefinitionKey
        )
    }
}
