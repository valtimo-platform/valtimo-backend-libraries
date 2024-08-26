package com.ritense.exact.client.endpoints

import com.fasterxml.jackson.annotation.JsonProperty
import com.ritense.exact.client.endpoints.ExchangeTokenEndpoint.ExchangeTokenResponse
import com.ritense.exact.client.endpoints.structs.ExactEndpoint
import org.springframework.web.client.RestClient
import org.springframework.web.reactive.function.BodyInserters

class ExchangeTokenEndpoint(
    private val redirectUrl: String,
    private val clientId: String,
    private val clientSecret: String,
    private val code: String
) : ExactEndpoint<ExchangeTokenResponse>(ExchangeTokenResponse::class.java) {

    override fun create(client: RestClient): RestClient.RequestHeadersSpec<*> {
        return client
            .post()
            .uri("/api/oauth2/token")
            .body(
                BodyInserters
                    .fromFormData("grant_type", "authorization_code")
                    .with("client_id", clientId)
                    .with("client_secret", clientSecret)
                    .with("code", code)
                    .with("redirect_uri", redirectUrl)
            )
    }

    data class ExchangeTokenResponse(
        @JsonProperty("refresh_token")
        val refreshToken: String,
        @JsonProperty("access_token")
        val accessToken: String,
        @JsonProperty("expires_in")
        val expiresIn: Number
    )
}



