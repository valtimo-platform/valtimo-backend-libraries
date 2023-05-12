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

package com.ritense.objectsapi.service

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.util.UUID

class ObjectsApiServiceTest {

    lateinit var objectsApiService: ObjectsApiService
    lateinit var server: MockWebServer
    lateinit var executedRequests: MutableList<RecordedRequest>


    @BeforeEach
    internal fun setUp() {
        startMockServer()
        objectsApiService = ObjectsApiService(
            ObjectsApiProperties(
                objectsApi = ServerAuthSpecification(server.url("").toString(), "token")
            )
        )
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should retrieve a full list of objects using page requests`() {
        val objects = objectsApiService.getObjects(URI("https://localhost/type"))

        assertEquals(2, objects.size)
        assertEquals(2, executedRequests.count { request -> request.path!!.startsWith("/api/v2/objects") })
    }

    fun startMockServer() {
        executedRequests = mutableListOf()

        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                executedRequests.add(request)
                val uri = UriComponentsBuilder.fromUriString(server.url("").toString() + request.path).build()
                val response = when (request.method + " " + uri.path) {
                    "GET /api/v2/objects" -> createGetObjectsResponse(uri)
                    else -> MockResponse().setResponseCode(404)
                }
                return response
            }
        }
        server = MockWebServer()
        server.dispatcher = dispatcher
        server.start()
    }

    private fun createGetObjectsResponse(uri: UriComponents): MockResponse {
        val pageNumber = uri.queryParams["page"]?.first()
        val nextPage = if (pageNumber == "1") {
            UriComponentsBuilder.fromUri(uri.toUri()).replaceQueryParam("page", 2).toUriString()
        } else null

        val previousPage = if (pageNumber == "2") {
            UriComponentsBuilder.fromUri(uri.toUri()).replaceQueryParam("page", 1).toUriString()
        } else null

        val objectId = UUID.randomUUID()
        val body = """
            {
                "count": 1,
                "next": ${nextPage?.let { "\"$it\"" }},
                "previous": ${previousPage?.let { "\"$it\"" }},
                "results": [
                    {
                        "url": "${server.url("/api/v2/objects/$objectId")}",
                        "uuid": "$objectId",
                        "type": "http://example.com",
                        "record": {}
                    }
                ]
            }
        """.trimIndent()

        return mockResponse(body)
    }

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }
}