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
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.support.HttpRequestWrapper
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate
import java.io.InputStream
import java.net.URI
import kotlin.text.Charsets.UTF_8

@Component
@SkipComponentScan
class HostDockerInternalRestClientCustomizer(
    private val dockerPorts: List<Int>,
    private val rewriteRequestHost: Boolean,
    private val webServerPort: Int,
) : RestClientCustomizer, RestTemplateCustomizer, ClientHttpRequestInterceptor {

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
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        val newBody = if (request.headers.contentType == APPLICATION_JSON) {
            modifyRequestBody(request, requestBody)
        } else {
            requestBody
        }

        val newRequest = modifyRequest(request, newBody)

        val response = execution.execute(newRequest, newBody)

        return if (response.headers.contentType == APPLICATION_JSON) {
            modifyResponse(request, response)
        } else {
            response
        }
    }

    private fun modifyRequestBody(
        request: HttpRequest,
        requestBody: ByteArray,
    ): ByteArray {
        return replaceLocalhost(requestBody.decodeToString(), request.uri.port).toByteArray()
    }

    private fun modifyRequest(
        request: HttpRequest,
        newBody: ByteArray,
    ): HttpRequest {
        return object : HttpRequestWrapper(request) {
            override fun getURI(): URI {
                val oldUri = super.getURI()
                val newHostUri =
                    if (dockerPorts.contains(webServerPort) && oldUri.port != webServerPort) {
                        if (oldUri.host == LOCALHOST) {
                            oldUri.toString().replaceFirst(LOCALHOST, HOST_DOCKER_INTERNAL)
                        } else {
                            oldUri.toString()
                        }
                    } else {
                        if (oldUri.host == HOST_DOCKER_INTERNAL) {
                            oldUri.toString().replaceFirst(HOST_DOCKER_INTERNAL, LOCALHOST)
                        } else {
                            oldUri.toString()
                        }
                    }
                val newUri =
                    if (oldUri.rawQuery != null) {
                        val newRawQuery = replaceLocalhost(oldUri.rawQuery, request.uri.port)
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

    private fun modifyResponse(
        request: HttpRequest,
        response: ClientHttpResponse,
    ): ClientHttpResponseWrapper {
        return object : ClientHttpResponseWrapper(response) {
            override fun getBody(): InputStream {
                return if (dockerPorts.contains(webServerPort) && request.uri.port != webServerPort) {
                    FindReplaceInputStream(
                        inputStream = super.getBody(),
                        oldValue = HTTP_LOCALHOST,
                        newValue = HTTP_HOST_DOCKER_INTERNAL,
                    )
                } else {
                    FindReplaceInputStream(
                        inputStream = super.getBody(),
                        oldValue = HTTP_HOST_DOCKER_INTERNAL,
                        newValue = HTTP_LOCALHOST,
                    )
                }
            }
        }
    }

    private fun replaceLocalhost(
        stringContainingLocalhost: String,
        requestPort: Int,
    ): String {
        return stringContainingLocalhost.replace(Regex("""http://localhost:([0-9]{4,5})""")) { match ->
            val port = match.groupValues[1].toInt()
            if ((rewriteRequestHost || port != requestPort) &&
                (dockerPorts.contains(port) || dockerPorts.contains(requestPort))
            ) {
                "$HTTP_HOST_DOCKER_INTERNAL:$port"
            } else {
                match.value
            }
        }
    }

    companion object {
        const val HTTP_HOST_DOCKER_INTERNAL = "http://host.docker.internal"
        const val HTTP_LOCALHOST = "http://localhost"
        const val HOST_DOCKER_INTERNAL = "host.docker.internal"
        const val LOCALHOST = "localhost"
    }
}