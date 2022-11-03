package com.ritense.exact.client.endpoints

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.exact.client.endpoints.structs.AuthorizedExactEndpoint
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec

class GetEndpoint(
    accessToken: String,
    private val uri: String
) : AuthorizedExactEndpoint<JsonNode>(JsonNode::class.java, accessToken) {
    override fun create(client: WebClient): RequestHeadersSpec<*> {
        return client
            .get()
            .uri(uri)
    }
}