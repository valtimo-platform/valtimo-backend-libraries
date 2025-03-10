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

import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.io.FindReplaceInputStream
import org.springframework.boot.web.client.RestClientCustomizer
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.support.HttpRequestWrapper
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.net.URI

@Component
@SkipComponentScan
class HostDockerInternalRestClientCustomizer(
    environment: Environment,
) : RestClientCustomizer, RestTemplateCustomizer, ClientHttpRequestInterceptor {

    private val isDevelopment = environment.activeProfiles.contains("dev")

    override fun customize(restClientBuilder: RestClient.Builder) {
        restClientBuilder.requestInterceptor(this)
    }

    override fun customize(restTemplate: RestTemplate) {
        if (restTemplate.interceptors.none { it is HostDockerInternalRestClientCustomizer }) {
            restTemplate.interceptors.add(this)
        }
    }
    
    override fun intercept(
        request: HttpRequest,
        requestBody: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        val newBody = if (isDevelopment && request.headers.contentType == APPLICATION_JSON) {
            modifyRequestBody(request, requestBody)
        } else {
            requestBody
        }

        val newRequest = if (isDevelopment) {
            modifyRequest(request, newBody)
        } else {
            request
        }

        val response = execution.execute(newRequest, newBody)

        return if (isDevelopment && response.headers.contentType == APPLICATION_JSON) {
            modifyResponse(response)
        } else {
            response
        }
    }

    private fun modifyRequestBody(
        request: HttpRequest,
        requestBody: ByteArray
    ): ByteArray {
        return requestBody
            .decodeToString()
            .replace(Regex("""http://localhost(?=:[0-9]{4})(?!:${request.uri.port})"""), HTTP_HOST_DOCKER_INTERNAL)
            .toByteArray()
    }

    private fun modifyRequest(
        request: HttpRequest,
        newBody: ByteArray
    ): HttpRequest {
        return object : HttpRequestWrapper(request) {
            override fun getURI(): URI {
                val oldUri = super.getURI()
                val newHostUri = if (oldUri.host == HOST_DOCKER_INTERNAL) {
                    oldUri.toString().replaceFirst(HOST_DOCKER_INTERNAL, LOCALHOST)
                } else {
                    oldUri.toString()
                }
                val newUri = if (oldUri.rawQuery != null) {
                    val newRawQuery = oldUri.rawQuery.replace(
                        Regex("""http://localhost(?=:[0-9]{4})(?!:${request.uri.port})"""),
                        HTTP_HOST_DOCKER_INTERNAL
                    )
                    newHostUri.replaceFirst(oldUri.rawQuery, newRawQuery)
                } else {
                    newHostUri
                }
                return URI(newUri)
            }

            override fun getHeaders(): HttpHeaders {
                val headers = HttpHeaders()
                headers.addAll(super.getHeaders())
                if (headers.contains("Content-Length")) {
                    headers["Content-Length"] = newBody.size.toString()
                }
                return headers
            }
        }
    }

    private fun modifyResponse(response: ClientHttpResponse): ClientHttpResponseWrapper {
        return object : ClientHttpResponseWrapper(response) {
            override fun getBody() = FindReplaceInputStream(
                inputStream = super.getBody(),
                oldValue = HTTP_HOST_DOCKER_INTERNAL,
                newValue = HTTP_LOCALHOST
            )
        }
    }

    companion object {
        const val HTTP_HOST_DOCKER_INTERNAL = "http://host.docker.internal"
        const val HTTP_LOCALHOST = "http://localhost"
        const val HOST_DOCKER_INTERNAL = "host.docker.internal"
        const val LOCALHOST = "localhost"
    }
}