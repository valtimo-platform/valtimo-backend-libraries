package com.ritense.valtimo.web.logging

import mu.KotlinLogging
import org.springframework.http.HttpStatusCode
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient

object RestClientLoggingExtension {

    private val logger = KotlinLogging.logger {}

    fun defaultRequestLogging(builder: RestClient.Builder): RestClient.Builder {
        return builder
            .defaultStatusHandler(
                { obj: HttpStatusCode -> obj.is4xxClientError || obj.is5xxServerError },
                { request, response ->
                    logger.error {
                        """
                        Request error report:
                        Method/Uri: '${request.method}' - '${request.uri}'
                        Header(s): '${request.headers}'
                        Status code: '${response.statusCode}'
                        Body content: '${String(response.body.readAllBytes())}'
                        """
                    }
                    throw HttpClientErrorException(response.statusCode)
                })
            .defaultStatusHandler(
                { obj: HttpStatusCode -> obj.is2xxSuccessful },
                { request, response ->
                    logger.debug {
                        """
                        Debug request report:
                        Method/Uri: '${request.method}' - '${request.uri}'
                        Header(s): '${request.headers}'
                        Status code: '${response.statusCode}'
                        Body content: '${String(response.body.readAllBytes())}'
                        """
                    }
                }
            )
    }
}

fun String.trimEmptyLines() = trim().replace("\n+".toRegex(), replacement = "\n")