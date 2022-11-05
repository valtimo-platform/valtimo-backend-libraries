package com.ritense.exact.client.endpoints.structs

import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec
import org.springframework.web.reactive.function.client.WebClientResponseException

abstract class ExactEndpoint<Response>(val type: Class<Response>) {

    open fun call(client: WebClient): Response {
        try {
            return create(client)
                .retrieve()
                .bodyToMono(type)
                .block()!!
        } catch (e: WebClientResponseException) {
            throw HttpClientErrorException(e.statusCode, e.responseBodyAsString)
        }
    }

    abstract fun create(client: WebClient): RequestHeadersSpec<*>

}