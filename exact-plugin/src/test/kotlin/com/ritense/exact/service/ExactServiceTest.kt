package com.ritense.exact.service

import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.ritense.exact.config.ExactPluginAutoConfiguration
import com.ritense.exact.plugin.ExactPlugin
import com.ritense.exact.service.request.ExactExchangeRequest
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginService
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.apache.coyote.http11.Constants.a
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration
import java.time.LocalDateTime

@TestInstance(Lifecycle.PER_CLASS)
internal class ExactServiceTest {

    lateinit var mockWebServer: MockWebServer
    lateinit var exactService: ExactService
    lateinit var exactClient: WebClient
    lateinit var pluginService: PluginService
    lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        exactClient = ExactPluginAutoConfiguration().exactClient("http://localhost:${mockWebServer.port}")
        pluginService = mock()
        objectMapper = JsonMapper.builder().findAndAddModules().build()
        exactService = ExactService("", exactClient, pluginService, objectMapper)

    }

    @BeforeAll
    fun setUpAll() {
        mockWebServer = MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testExactServiceExchange() {
        val request = ExactExchangeRequest(
            "ID",
            "Secret",
            "Code"
        )

        mockWebServer.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                {
                    "access_token": "AccessToken",
                    "refresh_token": "RefreshToken",
                    "expires_in": 600
                }
            """.trimIndent()
                )
        )

        val response = exactService.exchange(request)

        assertEquals("AccessToken", response.accessToken)
        assertEquals("RefreshToken", response.refreshToken)
        val diffInSeconds = Duration.between(LocalDateTime.now(), response.accessTokenExpiresOn).seconds;
        assertTrue(diffInSeconds in 599..601)
        val diffInDays = Duration.between(LocalDateTime.now(), response.refreshTokenExpiresOn).toDays();
        assertTrue(diffInDays in 29..31)
    }

    @Test
    fun testExactServiceRefreshAccessToken() {
        val pluginConfiguration: PluginConfiguration = mock()
        val objectNode: ObjectNode = mock()
        val pluginConfigurationId: PluginConfigurationId = mock()
        val now = LocalDateTime.now().plusMinutes(10)

        whenever(pluginConfiguration.properties).thenReturn(objectNode)
        whenever(pluginConfiguration.id).thenReturn(pluginConfigurationId)
        whenever(objectNode.get(eq("accessToken"))).thenReturn(TextNode("AccessToken"))
        whenever(objectNode.get(eq("accessTokenExpiresOn"))).thenReturn(ArrayNode(objectMapper.nodeFactory, listOf(
            IntNode(now.year),
            IntNode(now.monthValue),
            IntNode(now.dayOfMonth),
            IntNode(now.hour),
            IntNode(now.minute),
            IntNode(now.second),
            IntNode(now.nano)
        )))

        val response = exactService.refreshAccessTokens(pluginConfiguration)

        assertEquals("AccessToken", response)
    }

    @Test
    fun testExactServiceRefreshExpiredAccessToken() {
        val pluginConfiguration: PluginConfiguration = mock()
        val objectNode: ObjectNode = mock()
        val pluginConfigurationId: PluginConfigurationId = mock()
        val now = LocalDateTime.now()

        mockWebServer.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                {
                    "access_token": "AccessToken",
                    "refresh_token": "RefreshToken",
                    "expires_in": 600
                }
            """.trimIndent()
                )
        )

        whenever(pluginConfiguration.properties).thenReturn(objectNode)
        whenever(pluginConfiguration.id).thenReturn(pluginConfigurationId)
        whenever(pluginConfiguration.title).thenReturn("title")
        whenever(objectNode.get(eq("accessToken"))).thenReturn(TextNode("AccessToken"))
        whenever(objectNode.get(eq("clientId"))).thenReturn(TextNode("ClientId"))
        whenever(objectNode.get(eq("clientSecret"))).thenReturn(TextNode("ClientSecret"))
        whenever(objectNode.get(eq("refreshToken"))).thenReturn(TextNode("RefreshToken"))
        whenever(objectNode.get(eq("accessTokenExpiresOn"))).thenReturn(ArrayNode(objectMapper.nodeFactory, listOf(
            IntNode(now.year),
            IntNode(now.monthValue),
            IntNode(now.dayOfMonth),
            IntNode(now.hour),
            IntNode(now.minute),
            IntNode(now.second),
            IntNode(now.nano)
        )))
        whenever(pluginService.updatePluginConfiguration(any(), any(), any())).thenReturn(pluginConfiguration)


        val response = exactService.refreshAccessTokens(pluginConfiguration)

        verify(objectNode).put(eq("accessToken"), eq("AccessToken"))
        verify(objectNode).put(eq("refreshToken"), eq("RefreshToken"))
        verify(objectNode).putPOJO(eq("accessTokenExpiresOn"), any<LocalDateTime>())
        verify(objectNode).putPOJO(eq("refreshTokenExpiresOn"), any<LocalDateTime>())
        verify(objectNode).get(eq("accessToken"))

        assertEquals("AccessToken", response)
    }

    @Test
    fun testExactServiceGetPluginConfiguration() {
        val plugin: ExactPlugin = mock()
        val pluginConfiguration: PluginConfiguration = mock()
        val objectNode: ObjectNode = mock()

        whenever(pluginService.getPluginConfigurations(any())).thenReturn(listOf(pluginConfiguration))
        whenever(plugin.clientId).thenReturn("ClientId")
        whenever(pluginConfiguration.properties).thenReturn(objectNode)
        whenever(objectNode.get(eq("clientId"))).thenReturn(TextNode("ClientId"))

        var configuration = exactService.getPluginConfiguration(plugin)

        assertEquals(pluginConfiguration, configuration)
    }

}