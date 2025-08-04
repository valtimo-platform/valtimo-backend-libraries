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

package com.ritense.valtimo.contract.client

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.core.env.Environment
import org.springframework.http.HttpRequest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.mock.http.client.MockClientHttpRequest
import org.springframework.mock.http.client.MockClientHttpResponse
import java.net.URI
import kotlin.test.assertEquals

class HostDockerInternalRestClientCustomizerTest {

    lateinit var environment: Environment
    lateinit var hostDockerInternalRestClientCustomizer: HostDockerInternalRestClientCustomizer

    @BeforeEach
    fun beforeEach() {
        environment = mock()
        whenever(environment.activeProfiles).thenReturn(arrayOf("dev"))
        hostDockerInternalRestClientCustomizer = HostDockerInternalRestClientCustomizer(
            dockerPorts = listOf(8001, 8002, 8010, 8011),
            rewriteRequestHost = false,
            webServerPort = 8080,
        )
    }

    @Test
    fun `should replace request uri`() {
        val request = MockClientHttpRequest()
        request.headers.contentType = APPLICATION_JSON
        request.uri =
            URI("http://host.docker.internal:8010/api/v2/objects?type=http://localhost:8011/api/v1/objecttypes/feeaa795-d212-4fa2-bb38-2c34996e5702&pageSize=10&page=1")
        val requestBody = ByteArray(0)
        val execution = mock<ClientHttpRequestExecution>()
        val response = MockClientHttpResponse()
        response.headers.contentType = APPLICATION_JSON
        whenever(execution.execute(any(), any())).thenReturn(response)

        hostDockerInternalRestClientCustomizer.intercept(request, requestBody, execution)

        val captor = argumentCaptor<HttpRequest>()
        verify(execution).execute(captor.capture(), any())
        assertEquals(
            "http://localhost:8010/api/v2/objects?type=http://host.docker.internal:8011/api/v1/objecttypes/feeaa795-d212-4fa2-bb38-2c34996e5702&pageSize=10&page=1",
            captor.firstValue.uri.toString()
        )
    }

    @Test
    fun `should not replace request uri when matching port`() {
        val request = MockClientHttpRequest()
        request.headers.contentType = APPLICATION_JSON
        request.uri =
            URI("http://localhost:8001/catalogi/api/v1/zaaktype-informatieobjecttypen?zaaktype=http://localhost:8001/catalogi/api/v1/zaaktypen/744ca059-f412-49d4-8963-5800e4afd486&page=1")
        val requestBody = ByteArray(0)
        val execution = mock<ClientHttpRequestExecution>()
        val response = MockClientHttpResponse()
        response.headers.contentType = APPLICATION_JSON
        whenever(execution.execute(any(), any())).thenReturn(response)

        hostDockerInternalRestClientCustomizer.intercept(request, requestBody, execution)

        val captor = argumentCaptor<HttpRequest>()
        verify(execution).execute(captor.capture(), any())
        assertEquals(
            "http://localhost:8001/catalogi/api/v1/zaaktype-informatieobjecttypen?zaaktype=http://localhost:8001/catalogi/api/v1/zaaktypen/744ca059-f412-49d4-8963-5800e4afd486&page=1",
            captor.firstValue.uri.toString()
        )
    }

    @Test
    fun `should replace request body`() {
        val request = MockClientHttpRequest()
        request.headers.contentType = APPLICATION_JSON
        request.uri = URI("http://localhost:8010/api/v2/objects/f710ad49-8c90-4b4d-bf94-83555212dd5c")
        val requestBody = """
           {
              "uuid":"f710ad49-8c90-4b4d-bf94-83555212dd5c",
              "url":"http://localhost:8010/api/v2/objects/f710ad49-8c90-4b4d-bf94-83555212dd5c",
              "type":"http://localhost:8011/api/v1/objecttypes/feeaa795-d212-4fa2-bb38-2c34996e5702",
              "record":{
                 "data":{
                    "boomgroep":"Boomweide"
                 }
              }
           }
        """.trimIndent().toByteArray()
        val execution = mock<ClientHttpRequestExecution>()
        val response = MockClientHttpResponse()
        response.headers.contentType = APPLICATION_JSON
        whenever(execution.execute(any(), any())).thenReturn(response)

        hostDockerInternalRestClientCustomizer.intercept(request, requestBody, execution)

        val captor = argumentCaptor<ByteArray>()
        verify(execution).execute(any(), captor.capture())
        assertEquals(
            """
           {
              "uuid":"f710ad49-8c90-4b4d-bf94-83555212dd5c",
              "url":"http://localhost:8010/api/v2/objects/f710ad49-8c90-4b4d-bf94-83555212dd5c",
              "type":"http://host.docker.internal:8011/api/v1/objecttypes/feeaa795-d212-4fa2-bb38-2c34996e5702",
              "record":{
                 "data":{
                    "boomgroep":"Boomweide"
                 }
              }
           }
            """.trimIndent(),
            captor.firstValue.decodeToString()
        )
    }

    @Test
    fun `should replace request body when request-uri is docker container`() {
        val request = MockClientHttpRequest()
        request.headers.contentType = APPLICATION_JSON
        request.uri = URI("http://localhost:8002/api/v1/abonnement")
        val requestBody = """
            {
                "callbackUrl": "http://localhost:8080/api/v1/notificatiesapi/callback",
                "auth": "aaa=",
                "kanalen": [
                    {
                        "filters": {},
                        "naam": "objecten"
                    }
                ]
            }
        """.trimIndent().toByteArray()
        val execution = mock<ClientHttpRequestExecution>()
        val response = MockClientHttpResponse()
        response.headers.contentType = APPLICATION_JSON
        whenever(execution.execute(any(), any())).thenReturn(response)

        hostDockerInternalRestClientCustomizer.intercept(request, requestBody, execution)

        val captor = argumentCaptor<ByteArray>()
        verify(execution).execute(any(), captor.capture())
        assertEquals(
            """
            {
                "callbackUrl": "http://host.docker.internal:8080/api/v1/notificatiesapi/callback",
                "auth": "aaa=",
                "kanalen": [
                    {
                        "filters": {},
                        "naam": "objecten"
                    }
                ]
            }
        """.trimIndent(),
            captor.firstValue.decodeToString()
        )
    }

    @Test
    fun `should replace response body`() {
        val request = MockClientHttpRequest()
        request.headers.contentType = APPLICATION_JSON
        request.uri =
            URI("http://localhost:8010/api/v2/objects?type=http://host.docker.internal:8011/api/v1/objecttypes/feeaa795-d212-4fa2-bb38-2c34996e5702&pageSize=10&page=1")
        val requestBody = ByteArray(0)
        val execution = mock<ClientHttpRequestExecution>()
        val responseBody = """
            {
               "count":1,
               "results":[
                  {
                     "url":"http://localhost:8010/api/v2/objects/f710ad49-8c90-4b4d-bf94-83555212dd5c",
                     "uuid":"f710ad49-8c90-4b4d-bf94-83555212dd5c",
                     "type":"http://host.docker.internal:8011/api/v1/objecttypes/feeaa795-d212-4fa2-bb38-2c34996e5702",
                     "record":{
                        "data":{
                           "boomgroep":"Boomweide"
                        }
                     }
                  }
               ]
            }
        """.trimIndent()
        val response = MockClientHttpResponse(responseBody.toByteArray(), 200)
        response.headers.contentType = APPLICATION_JSON
        response.body
        whenever(execution.execute(any(), any())).thenReturn(response)

        val result = hostDockerInternalRestClientCustomizer.intercept(request, requestBody, execution)

        assertEquals(
            """
            {
               "count":1,
               "results":[
                  {
                     "url":"http://localhost:8010/api/v2/objects/f710ad49-8c90-4b4d-bf94-83555212dd5c",
                     "uuid":"f710ad49-8c90-4b4d-bf94-83555212dd5c",
                     "type":"http://localhost:8011/api/v1/objecttypes/feeaa795-d212-4fa2-bb38-2c34996e5702",
                     "record":{
                        "data":{
                           "boomgroep":"Boomweide"
                        }
                     }
                  }
               ]
            }
            """.trimIndent(),
            result.body.readAllBytes().decodeToString()
        )
    }

    @Test
    fun `should replace localhost with 'host-docker-internal' in request and response when inside docker container`() {
        hostDockerInternalRestClientCustomizer = HostDockerInternalRestClientCustomizer(
            dockerPorts = listOf(8080, 8001, 8002, 8010, 8011),
            rewriteRequestHost = false,
            webServerPort = 8080,
        )

        val request = MockClientHttpRequest()
        request.headers.contentType = APPLICATION_JSON
        request.uri =
            URI("http://localhost:8010/api/v2/objects?type=http://localhost:8011/api/v1/objecttypes/feeaa795-d212-4fa2-bb38-2c34996e5702&pageSize=10&page=1")
        val requestBody = ByteArray(0)
        val execution = mock<ClientHttpRequestExecution>()
        val responseBody = """
            {
               "count":1,
               "results":[
                  {
                     "url":"http://localhost:8010/api/v2/objects/f710ad49-8c90-4b4d-bf94-83555212dd5c",
                     "uuid":"f710ad49-8c90-4b4d-bf94-83555212dd5c",
                     "type":"http://host.docker.internal:8011/api/v1/objecttypes/feeaa795-d212-4fa2-bb38-2c34996e5702",
                     "record":{
                        "data":{
                           "boomgroep":"Boomweide"
                        }
                     }
                  }
               ]
            }
        """.trimIndent()
        val response = MockClientHttpResponse(responseBody.toByteArray(), 200)
        response.headers.contentType = APPLICATION_JSON
        response.body
        whenever(execution.execute(any(), any())).thenReturn(response)

        val result = hostDockerInternalRestClientCustomizer.intercept(request, requestBody, execution)

        val captor = argumentCaptor<HttpRequest>()
        verify(execution).execute(captor.capture(), any())
        assertEquals(
            "http://host.docker.internal:8010/api/v2/objects?type=http://host.docker.internal:8011/api/v1/objecttypes/feeaa795-d212-4fa2-bb38-2c34996e5702&pageSize=10&page=1",
            captor.firstValue.uri.toString()
        )
        assertEquals(
            """
            {
               "count":1,
               "results":[
                  {
                     "url":"http://host.docker.internal:8010/api/v2/objects/f710ad49-8c90-4b4d-bf94-83555212dd5c",
                     "uuid":"f710ad49-8c90-4b4d-bf94-83555212dd5c",
                     "type":"http://host.docker.internal:8011/api/v1/objecttypes/feeaa795-d212-4fa2-bb38-2c34996e5702",
                     "record":{
                        "data":{
                           "boomgroep":"Boomweide"
                        }
                     }
                  }
               ]
            }
            """.trimIndent(),
            result.body.readAllBytes().decodeToString()
        )
    }

}