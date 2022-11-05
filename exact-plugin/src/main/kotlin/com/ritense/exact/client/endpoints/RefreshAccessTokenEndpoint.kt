package com.ritense.exact.client.endpoints

import com.fasterxml.jackson.annotation.JsonProperty
import com.ritense.exact.client.endpoints.RefreshAccessTokenEndpoint.RefreshAccessTokenResponse
import com.ritense.exact.client.endpoints.structs.ExactEndpoint
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec

class RefreshAccessTokenEndpoint(
    private val clientId: String,
    private val clientSecret: String,
    private val refreshToken: String
) : ExactEndpoint<RefreshAccessTokenResponse>(RefreshAccessTokenResponse::class.java) {

    override fun create(client: WebClient): RequestHeadersSpec<*> {
        return client.post()
            .uri("/api/oauth2/token")
            .body(
                BodyInserters
                    .fromFormData("grant_type", "refresh_token")
                    .with("client_id", clientId)
                    .with("client_secret", clientSecret)
                    .with("refresh_token", refreshToken)
            )
    }

    data class RefreshAccessTokenResponse(
        @JsonProperty("refresh_token")
        val refreshToken: String,
        @JsonProperty("access_token")
        val accessToken: String,
        @JsonProperty("expires_in")
        val expiresIn: Number
    )

}

