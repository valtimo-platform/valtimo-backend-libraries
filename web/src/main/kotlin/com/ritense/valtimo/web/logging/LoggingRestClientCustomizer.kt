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

package com.ritense.valtimo.web.logging

import com.ritense.valtimo.contract.annotation.SkipComponentScan
import mu.KotlinLogging
import org.springframework.boot.web.client.RestClientCustomizer
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import kotlin.text.Charsets.UTF_8

@Component
@SkipComponentScan
class LoggingRestClientCustomizer : RestClientCustomizer, ClientHttpRequestInterceptor {

    override fun customize(restClientBuilder: RestClient.Builder) {
        restClientBuilder.requestInterceptor(this)
    }

    override fun intercept(
        request: HttpRequest,
        requestBody: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        val response = execution.execute(request, requestBody)
        val requestBodyHead = requestBody.take(DEFAULT_BUFFER_SIZE).toByteArray()
        return CopiedHeadClientHttpResponse(response) { responseBodyHead ->
            logger.debug { createRequestReport(request, requestBodyHead, response, responseBodyHead) }
            if (response.statusCode.isError) {
                val report = createRequestReport(request, requestBodyHead, response, responseBodyHead)
                throw HttpClientErrorException(
                    report,
                    response.statusCode,
                    response.statusText,
                    response.headers,
                    responseBodyHead,
                    UTF_8
                )
            }
        }
    }

    private fun createRequestReport(
        request: HttpRequest,
        requestBody: ByteArray,
        response: ClientHttpResponse,
        responseBody: ByteArray
    ): String {
        return """
            Request:
            HTTP Method = ${request.method}
            Request URI = ${request.uri}
            Headernames = ${request.headers.keys}
            Body = ${String(requestBody)}
            ---------------------------------------
            Response:
            Status = ${response.statusCode}
            Headers = ${response.headers}
            Content type = ${response.headers.contentType}
            Body = ${String(responseBody)}
        """.trimIndent()
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }

}