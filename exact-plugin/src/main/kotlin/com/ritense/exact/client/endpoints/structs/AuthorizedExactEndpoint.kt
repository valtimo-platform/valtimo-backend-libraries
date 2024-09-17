package com.ritense.exact.client.endpoints.structs

import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

abstract class AuthorizedExactEndpoint<Response>(
    type: Class<Response>,
    private val accessToken: String
) : ExactEndpoint<Response>(type) {

    override fun call(client: RestClient): Response {
        try {
            return create(client)
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .body(type)!!
        } catch (e: RestClientResponseException) {
            throw HttpClientErrorException(e.statusCode, e.responseBodyAsString)
        }
    }

}