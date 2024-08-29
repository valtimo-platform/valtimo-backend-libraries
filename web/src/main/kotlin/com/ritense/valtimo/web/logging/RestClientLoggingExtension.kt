package com.ritense.valtimo.web.logging

import mu.KotlinLogging
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient

object RestClientLoggingExtension {

    private val logger = KotlinLogging.logger {}

    fun defaultRequestLogging(builder: RestClient.Builder): RestClient.Builder {
        return builder
            .defaultStatusHandler(
                { obj: HttpStatusCode -> obj.is4xxClientError || obj.is5xxServerError },
                { request, response ->
                    val report = createRequestReport(request, response)
                    logger.error { report } // Really needed? are errors already logged?
                    throw HttpClientErrorException(response.statusCode, report)
                }
            )
            .defaultStatusHandler(
                { obj: HttpStatusCode -> obj.is2xxSuccessful },
                { request, response -> logger.debug { createRequestReport(request, response) } }
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
/*
*
* MockHttpServletRequest:
      HTTP Method = POST
      Request URI = /api/v1/openzaak/informatie-object-type-link
       Parameters = {}
          Headers = [Content-Type:"application/json;charset=UTF-8", Accept:"application/json", Content-Length:"123"]
             Body = {"documentDefinitionName":"name","zaakType":"http://zaaktype.com","informatieObjectType":"http://informatieobjecttype.com"}
    Session Attrs = {}
* */