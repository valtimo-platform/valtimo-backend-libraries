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

package com.ritense.smartdocuments

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.plugin.repository.PluginActionDefinitionRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import com.ritense.plugin.repository.PluginPropertyRepository
import com.ritense.smartdocuments.config.SmartDocumentsAuthentication
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod

class BaseSmartDocumentsIntegrationTest : BaseIntegrationTest() {

    @Autowired
    lateinit var pluginDefinitionRepository: PluginDefinitionRepository

    @Autowired
    lateinit var pluginPropertyRepository: PluginPropertyRepository

    @Autowired
    lateinit var pluginActionDefinitionRepository: PluginActionDefinitionRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    lateinit var server: MockWebServer
    lateinit var executedRequests: MutableList<RecordedRequest>

    @BeforeEach
    internal fun setUp() {
        startMockServer()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    fun startMockServer() {
        executedRequests = mutableListOf()
        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                executedRequests.add(request)
                val response = when (request.path?.substringBefore('?')) {
                    "/wsxmldeposit/deposit/unattended" -> when (request.method) {
                        "POST" -> mockResponseFromFile("/data/post-generate-document.json")
                        else -> MockResponse().setResponseCode(404)
                    }
                    else -> MockResponse().setResponseCode(404)
                }
                return response
            }
        }
        server = MockWebServer()
        server.dispatcher = dispatcher
        server.start()
    }

    fun findRequest(method: HttpMethod, path: String): RecordedRequest? {
        return executedRequests
            .filter { method.matches(it.method!!) }
            .firstOrNull { it.path?.substringBefore('?').equals(path) }
    }

    fun <T> findRequestBody(method: HttpMethod, path: String, clazz: Class<T>): T {
        return objectMapper.readValue(findRequest(method, path)!!.body.readUtf8(), clazz)
    }

    private fun mockResponseFromFile(fileName: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(200)
            .setBody(readFileAsString(fileName))
    }

    fun getSmartDocumentsAuthentication(): SmartDocumentsAuthentication {
        return SmartDocumentsAuthentication(
            url = server.url("/").toString(),
            username = "valtimo-test",
            password = "41625e22-c4ef-487b-93fc-e46a25278d11"
        )
    }
}
