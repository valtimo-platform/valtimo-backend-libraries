package com.ritense.exact.client.endpoints.structs

import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

abstract class ExactEndpoint<Response>(val type: Class<Response>) {

    open fun call(client: RestClient): Response {
        try {
            return create(client)
                .retrieve()
                .body(type)!!
        } catch (e: RestClientResponseException) {
            throw HttpClientErrorException(e.statusCode, e.responseBodyAsString)
        }
    }

    abstract fun create(client: RestClient): RestClient.RequestHeadersSpec<*>

}