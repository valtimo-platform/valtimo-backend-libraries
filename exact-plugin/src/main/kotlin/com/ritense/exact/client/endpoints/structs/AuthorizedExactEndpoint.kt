package com.ritense.exact.client.endpoints.structs

import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

abstract class AuthorizedExactEndpoint<Response>(
    type: Class<Response>,
    private val accessToken: String
): ExactEndpoint<Response>(type) {

    override fun call(client: WebClient): Response {
        try {
            return create(client)
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .bodyToMono(type)
                .block()!!
        } catch (e: WebClientResponseException) {
            throw HttpClientErrorException(e.statusCode, e.responseBodyAsString)
        }
    }

}