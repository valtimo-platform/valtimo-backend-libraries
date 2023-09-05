package com.ritense.exact.client.endpoints

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.exact.client.endpoints.structs.AuthorizedExactEndpoint
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec

class PutEndpoint(
    accessToken: String,
    private val uri: String,
    private val content: String
) : AuthorizedExactEndpoint<JsonNode>(JsonNode::class.java, accessToken) {
    override fun create(client: WebClient): RequestHeadersSpec<*> {
        return client
            .put()
            .uri(uri)
            .body(BodyInserters.fromValue(content))
    }
}