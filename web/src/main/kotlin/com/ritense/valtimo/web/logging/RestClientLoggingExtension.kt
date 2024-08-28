package com.ritense.valtimo.web.logging

import mu.KotlinLogging
import org.springframework.http.HttpStatusCode
import org.springframework.web.client.RestClient

object RestClientLoggingExtension {

    private val logger = KotlinLogging.logger {}

    fun defaultRequestLogging(builder: RestClient.Builder): RestClient.Builder {
        return builder
            .defaultStatusHandler(
                { obj: HttpStatusCode -> obj.is4xxClientError || obj.is5xxServerError },
                { request, response ->
                    logger.error { "Request info ${request.uri} ${request.method}" }
                    logger.error { "Error Status ${response.statusCode}" }
                    logger.error { "Error Body " + String(response.body.readAllBytes()) }
                })
            .defaultStatusHandler(
                { obj: HttpStatusCode -> obj.is2xxSuccessful },
                { request, response ->
                    logger.debug { "Request info ${request.uri} ${request.method}" }
                    logger.debug { "Debugged request: Status ${response.statusCode}" }
                    logger.debug { "Debugged request " + String(response.body.readAllBytes()) }
                })
    }

}