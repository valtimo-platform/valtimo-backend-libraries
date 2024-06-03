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

package com.ritense.documentenapi

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.documentenapi.client.DocumentenApiClient
import com.ritense.documentenapi.service.DocumentDeleteHandler
import com.ritense.documentenapi.service.DocumentenApiVersionService
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProperty
import com.ritense.plugin.service.PluginService
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.valtimo.contract.json.MapperSingleton
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import java.net.URI

internal class DocumentenApiPluginFactoryTest {

    @Test
    fun `should create DocumentApiPlugin`() {
        val pluginService: PluginService = mock()
        val client: DocumentenApiClient = mock()
        val storageService: TemporaryResourceStorageService = mock()
        val authentication = mock<DocumentenApiAuthentication>()
        val applicationEventPublisher: ApplicationEventPublisher = mock()
        val objectMapper = MapperSingleton.get()
        val documentDeleteHandlers: List<DocumentDeleteHandler> = mock()
        val documentenApiVersionService: DocumentenApiVersionService = mock()
        whenever(pluginService.createInstance(any<PluginConfigurationId>())).thenReturn(authentication)
        whenever(pluginService.getObjectMapper()).thenReturn(MapperSingleton.get())

        val propertyString = """
          {
            "url": "http://some-url",
            "bronorganisatie": "123456789",
            "authenticationPluginConfiguration": "7dce138c-b113-4c2c-87a9-cfc3549b9438"
          }
        """.trimIndent()

        val propertyDefinitions = mutableSetOf<PluginProperty>()
        val pluginDefinition = PluginDefinition(
            "key",
            "title",
            "descriptioon",
            "class",
            propertyDefinitions
        )
        propertyDefinitions.add(PluginProperty("url", pluginDefinition, "title", required = true,
            secret = false, "url", "java.net.URI"))
        propertyDefinitions.add(PluginProperty("bronorganisatie", pluginDefinition, "title", required = true,
            secret = false, "bronorganisatie", "java.lang.String"))
        propertyDefinitions.add(PluginProperty("authenticationPluginConfiguration", pluginDefinition, "title",
            required = true, secret = false, "authenticationPluginConfiguration",
            "com.ritense.documentenapi.DocumentenApiAuthentication"))

        val configuration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            MapperSingleton.get().readTree(propertyString) as ObjectNode,
            pluginDefinition
        )

        val factory = DocumentenApiPluginFactory(
            pluginService,
            client,
            storageService,
            applicationEventPublisher,
            objectMapper,
            documentDeleteHandlers,
            documentenApiVersionService,
        )

        val plugin = factory.create(configuration)

        assertEquals(URI("http://some-url"), plugin.url)
        assertEquals("123456789", plugin.bronorganisatie)
        assertEquals(authentication, plugin.authenticationPluginConfiguration)
    }

}