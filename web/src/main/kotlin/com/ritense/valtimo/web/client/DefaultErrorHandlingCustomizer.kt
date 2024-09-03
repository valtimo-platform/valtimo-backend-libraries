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

package com.ritense.valtimo.web.client

import org.springframework.boot.web.client.RestClientCustomizer
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient

class DefaultErrorHandlingCustomizer : RestClientCustomizer {

    override fun customize(restClientBuilder: RestClient.Builder) {
        restClientBuilder.defaultStatusHandler(
            { code: HttpStatusCode -> code.is4xxClientError || code.is5xxServerError },
            { request, response ->
                val report = createRequestReport(request, response)
                throw HttpClientErrorException(response.statusCode, report)
            }
        )
    }

    private fun createRequestReport(request: HttpRequest, response: ClientHttpResponse): String {
        return """
            Request report:
            HTTP Method = ${request.method}
            Request URI = ${request.uri}
            Headers = ${request.headers}
            ---------------------------------------
            Response:
            Status = ${response.statusCode}
            Headers = ${response.headers}
            Content type = ${response.headers.contentType}
            Body = ${String(response.body.readAllBytes())}
        """
    }

}