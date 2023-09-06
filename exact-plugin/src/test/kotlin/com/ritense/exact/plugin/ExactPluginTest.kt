package com.ritense.exact.plugin

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.exact.client.endpoints.ExactGetRequest
import com.ritense.exact.client.endpoints.ExactPostRequest
import com.ritense.exact.client.endpoints.ExactPutRequest
import com.ritense.exact.client.endpoints.GetEndpoint
import com.ritense.exact.client.endpoints.PostEndpoint
import com.ritense.exact.client.endpoints.PutEndpoint
import com.ritense.exact.config.ExactPluginAutoConfiguration
import com.ritense.exact.plugin.ExactPlugin.ExactCallProperties
import com.ritense.exact.service.ExactService
import com.ritense.plugin.domain.PluginConfiguration
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
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
import org.springframework.context.ApplicationContext
import org.springframework.web.reactive.function.client.WebClient

@TestInstance(Lifecycle.PER_CLASS)
internal class ExactPluginTest {

    lateinit var mockWebServer: MockWebServer
    private lateinit var exactService: ExactService
    lateinit var context: ApplicationContext
    private lateinit var exactPlugin: ExactPlugin
    lateinit var exactClient: WebClient

    @BeforeAll
    fun setUpAll() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @AfterAll
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @BeforeEach
    fun setUpEach() {
        exactService = mock()
        context = mock()
        exactClient = ExactPluginAutoConfiguration().exactClient("http://localhost:${mockWebServer.port}")
        exactPlugin = ExactPlugin(exactService, exactClient, context)
    }

    @Test
    fun testExactPluginActionGetFixedUri() {
        val execution: DelegateExecution = mock()
        val properties: ExactCallProperties = mock()
        val pluginConfiguration: PluginConfiguration = mock()

        mockWebServer.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                {
                    "d": { "results": [ {
                        "a": 1
                    }]
                    }
                }
            """.trimIndent()
                )
        )

        whenever(exactService.getPluginConfiguration(eq(exactPlugin))).thenReturn(pluginConfiguration)
        whenever(exactService.refreshAccessTokens(eq(pluginConfiguration))).thenReturn("AccessToken")
        whenever(properties.uri).thenReturn("/api/test")

        exactPlugin.getCallExact(execution, properties)

        verify(execution).setVariable(eq("exactGetResult"), any<JsonNode>())

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("http://localhost:${mockWebServer.port}/api/test", request.requestUrl.toString())
    }

    class TestGetRequest : ExactGetRequest {
        override fun createRequest(execution: DelegateExecution, token: String): GetEndpoint {
            return GetEndpoint(token, "/api/test")
        }

        override fun handleResponse(execution: DelegateExecution, response: JsonNode) {
            execution.setVariable("test", response)
        }
    }

    @Test
    fun testExactPluginActionGetBean() {
        val execution: DelegateExecution = mock()
        val properties: ExactCallProperties = mock()
        val pluginConfiguration: PluginConfiguration = mock()

        mockWebServer.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                {
                    "d": { "results": [ {
                        "a": 1
                    }]
                    }
                }
            """.trimIndent()
                )
        )

        whenever(exactService.getPluginConfiguration(eq(exactPlugin))).thenReturn(pluginConfiguration)
        whenever(exactService.refreshAccessTokens(eq(pluginConfiguration))).thenReturn("AccessToken")
        whenever(properties.bean).thenReturn("beanName")
        whenever(context.getBean(eq("beanName"), eq(ExactGetRequest::class.java))).thenReturn(TestGetRequest())

        exactPlugin.getCallExact(execution, properties)

        verify(execution).setVariable(eq("test"), any<JsonNode>())

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("http://localhost:${mockWebServer.port}/api/test", request.requestUrl.toString())
    }

    @Test
    fun testExactPluginActionPostFixedUriAndContent() {
        val execution: DelegateExecution = mock()
        val properties: ExactCallProperties = mock()
        val pluginConfiguration: PluginConfiguration = mock()

        mockWebServer.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                {
                    "d": { "results": [ {
                        "a": 1
                    }]
                    }
                }
            """.trimIndent()
                )
        )

        whenever(exactService.getPluginConfiguration(eq(exactPlugin))).thenReturn(pluginConfiguration)
        whenever(exactService.refreshAccessTokens(eq(pluginConfiguration))).thenReturn("AccessToken")
        whenever(properties.uri).thenReturn("/api/test")
        whenever(properties.content).thenReturn(
            """
            { "request": "1" }
        """.trimIndent()
        )

        exactPlugin.postCallExact(execution, properties)

        verify(execution).setVariable(eq("exactPostResult"), any<JsonNode>())

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("http://localhost:${mockWebServer.port}/api/test", request.requestUrl.toString())
        assertEquals(
            """
            { "request": "1" }
        """.trimIndent(), request.body.readUtf8()
        )
    }

    class TestPostRequest : ExactPostRequest {
        override fun createRequest(execution: DelegateExecution, token: String): PostEndpoint {
            return PostEndpoint(
                token, "/api/test", """
                { "test": 1 }
            """.trimIndent()
            )
        }

        override fun handleResponse(execution: DelegateExecution, response: JsonNode) {
            execution.setVariable("test", response)
        }
    }

    @Test
    fun testExactPluginActionPostBean() {
        val execution: DelegateExecution = mock()
        val properties: ExactCallProperties = mock()
        val pluginConfiguration: PluginConfiguration = mock()

        mockWebServer.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                {
                    "d": { "results": [ {
                        "a": 1
                    }]
                    }
                }
            """.trimIndent()
                )
        )

        whenever(exactService.getPluginConfiguration(eq(exactPlugin))).thenReturn(pluginConfiguration)
        whenever(exactService.refreshAccessTokens(eq(pluginConfiguration))).thenReturn("AccessToken")
        whenever(properties.bean).thenReturn("beanName")
        whenever(context.getBean(eq("beanName"), eq(ExactPostRequest::class.java))).thenReturn(TestPostRequest())

        exactPlugin.postCallExact(execution, properties)

        verify(execution).setVariable(eq("test"), any<JsonNode>())

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("http://localhost:${mockWebServer.port}/api/test", request.requestUrl.toString())
        assertEquals(
            """
            { "test": 1 }
        """.trimIndent(), request.body.readUtf8()
        )
    }

    @Test
    fun testExactPluginActionPutFixedUriAndContent() {
        val execution: DelegateExecution = mock()
        val properties: ExactCallProperties = mock()
        val pluginConfiguration: PluginConfiguration = mock()

        mockWebServer.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                {
                    "d": { "results": [ {
                        "a": 1
                    }]
                    }
                }
            """.trimIndent()
                )
        )

        whenever(exactService.getPluginConfiguration(eq(exactPlugin))).thenReturn(pluginConfiguration)
        whenever(exactService.refreshAccessTokens(eq(pluginConfiguration))).thenReturn("AccessToken")
        whenever(properties.uri).thenReturn("/api/test")
        whenever(properties.content).thenReturn(
            """
            { "request": "1" }
        """.trimIndent()
        )

        exactPlugin.putCallExact(execution, properties)

        verify(execution).setVariable(eq("exactPutResult"), any<JsonNode>())

        val request = mockWebServer.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("http://localhost:${mockWebServer.port}/api/test", request.requestUrl.toString())
        assertEquals(
            """
            { "request": "1" }
        """.trimIndent(), request.body.readUtf8()
        )
    }

    class TestPutRequest : ExactPutRequest {
        override fun createRequest(execution: DelegateExecution, token: String): PutEndpoint {
            return PutEndpoint(
                token, "/api/test", """
                { "test": 1 }
            """.trimIndent()
            )
        }

        override fun handleResponse(execution: DelegateExecution, response: JsonNode) {
            execution.setVariable("test", response)
        }
    }

    @Test
    fun testExactPluginActionPutBean() {
        val execution: DelegateExecution = mock()
        val properties: ExactCallProperties = mock()
        val pluginConfiguration: PluginConfiguration = mock()

        mockWebServer.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                {
                    "d": { "results": [ {
                        "a": 1
                    }]
                    }
                }
            """.trimIndent()
                )
        )

        whenever(exactService.getPluginConfiguration(eq(exactPlugin))).thenReturn(pluginConfiguration)
        whenever(exactService.refreshAccessTokens(eq(pluginConfiguration))).thenReturn("AccessToken")
        whenever(properties.bean).thenReturn("beanName")
        whenever(context.getBean(eq("beanName"), eq(ExactPutRequest::class.java))).thenReturn(TestPutRequest())

        exactPlugin.putCallExact(execution, properties)

        verify(execution).setVariable(eq("test"), any<JsonNode>())

        val request = mockWebServer.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("http://localhost:${mockWebServer.port}/api/test", request.requestUrl.toString())
        assertEquals(
            """
            { "test": 1 }
        """.trimIndent(), request.body.readUtf8()
        )
    }
}