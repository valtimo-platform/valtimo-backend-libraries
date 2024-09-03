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