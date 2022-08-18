package com.ritense.zakenapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.document.service.DocumentService
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProperty
import com.ritense.plugin.service.PluginService
import com.ritense.zakenapi.client.ZakenApiClient
import org.junit.Test
import kotlin.test.assertEquals

internal class ZakenApiPluginFactoryTest {

    @Test
    fun `should create ZakenApiPlugin`() {
        val pluginService: PluginService = mock()
        val authenticationMock = mock<ZakenApiAuthentication>()
        whenever(pluginService.createInstance(any<PluginConfigurationId>())).thenReturn(authenticationMock)

        val client: ZakenApiClient = mock()
        val zaakUrlProvider: ZaakUrlProvider = mock()
        val documentService: DocumentService = mock()
        val resourceProvider: ResourceProvider = mock()

        val factory = ZakenApiPluginFactory(
            pluginService,
            client,
            zaakUrlProvider,
            resourceProvider,
            documentService
        )
        val zakenApiPluginProperties: String = """
            {
              "url": "http://zaken.plugin.url",
              "authenticationPluginConfiguration": "7c4e15e4-c245-4fd9-864b-dd36baa02abf"
            }
        """.trimIndent()

        val pluginDefinition = createPluginDefinition()
        val pluginConfiguration = PluginConfiguration(
            PluginConfigurationId.newId(),
            "title",
            ObjectMapper().readTree(zakenApiPluginProperties) as ObjectNode,
            pluginDefinition
        )

        val plugin = factory.create(pluginConfiguration)

        assertEquals("http://zaken.plugin.url", plugin.url.toString())
        assertEquals(authenticationMock, plugin.authenticationPluginConfiguration)
    }

    private fun createPluginDefinition(): PluginDefinition {
        val propertyDefinitions = mutableSetOf<PluginProperty>()
        val pluginDefinition = PluginDefinition(
            "key",
            "title",
            "description",
            "class",
            propertyDefinitions
        )

        propertyDefinitions.add(
            PluginProperty("url", pluginDefinition, "title", required = true,
            secret = false, "url","java.lang.String"))
        propertyDefinitions.add(PluginProperty("authenticationPluginConfiguration", pluginDefinition, "title",
            required = true, secret = false, "authenticationPluginConfiguration",
            "com.ritense.zakenapi.ZakenApiAuthentication"))

        return pluginDefinition
    }
}