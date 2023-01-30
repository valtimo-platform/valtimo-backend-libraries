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
import java.net.URI
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired

internal class VerzoekPluginFactoryIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var verzoekPluginFactory: VerzoekPluginFactory

    lateinit var notificatiesApiPluginConfiguration: PluginConfiguration

    lateinit var mockNotificatiesApi: MockWebServer

    @BeforeEach
    fun beforeEach() {
        notificatiesApiPluginConfiguration = mock()
        mockNotificatiesApi = MockWebServer()
        mockNotificatiesApi.start()


        mockNotificatiesApi.enqueue(
            mockResponse(
                """
            [
              {
                "url": "http://example.com",
                "naam": "objecten",
                "documentatieLink": "http://example.com",
                "filters": [
                  "objecten"
                ]
              }
            ]
        """.trimIndent()
            )
        )

        mockNotificatiesApi.enqueue(
            mockResponse(
                """
                    {
                      "url": "http://example.com/abonnement/test-abonnement",
                      "callbackUrl": "http://example.com/callback",
                      "auth": "Bearer token",
                      "kanalen": [
                        {
                          "filters": {
                            "url": "http://example.com",
                            "someid": "1234"
                          },
                          "naam": "objecten"
                        }
                      ]
                    }
        """.trimIndent()
            )
        )


        val notificatiesApiAuthenticationPluginConfiguration = createPluginConfiguration(
            "notificatiesapiauthentication", """
            {
              "clientId": "my-client-id",
              "clientSecret": "my-extra-long-client-secret-128370192641209486239846"
            }
        """.trimIndent()
        )

        notificatiesApiPluginConfiguration = createPluginConfiguration(
            "notificatiesapi", """
            {
              "url": "${mockNotificatiesApi.url("/api/v1/").toUri()}",
              "callbackUrl": "https://example.com/my-callback-api-endpoint",
              "authenticationPluginConfiguration": "${notificatiesApiAuthenticationPluginConfiguration.id.id}"
            }
        """.trimIndent()
        )
    }

    @AfterEach
    fun tearDown() {
        mockNotificatiesApi.shutdown()
    }

    @Test
    fun `should create VerzoekPlugin`() {
        val mockNotificatiesApiUrl = mockNotificatiesApi.url("/api/v1/").toUri()

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

        assertEquals(mockNotificatiesApiUrl, plugin.notificatiesApiPluginConfiguration.url)
        assertEquals(
            URI("https://example.com/my-callback-api-endpoint"),
            plugin.notificatiesApiPluginConfiguration.callbackUrl
        )
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

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

    private fun createPluginConfiguration(pluginDefinitionKey: String, pluginProperties: String): PluginConfiguration {
        return pluginService.createPluginConfiguration(
            "my-configuration-$pluginDefinitionKey-${pluginProperties.hashCode()}",
            jacksonObjectMapper().readTree(pluginProperties).deepCopy(),
            pluginDefinitionKey
        )
    }
}
