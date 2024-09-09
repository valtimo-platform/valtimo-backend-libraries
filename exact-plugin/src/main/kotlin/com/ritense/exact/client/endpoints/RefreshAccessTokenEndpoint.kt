package com.ritense.exact.client.endpoints

import com.fasterxml.jackson.annotation.JsonProperty
import com.ritense.exact.client.endpoints.RefreshAccessTokenEndpoint.RefreshAccessTokenResponse
import com.ritense.exact.client.endpoints.structs.ExactEndpoint
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.web.client.RestClient


class RefreshAccessTokenEndpoint(
    private val clientId: String,
    private val clientSecret: String,
    private val refreshToken: String
) : ExactEndpoint<RefreshAccessTokenResponse>(RefreshAccessTokenResponse::class.java) {

    override fun create(client: RestClient): RestClient.RequestHeadersSpec<*> {
        val builder = MultipartBodyBuilder().apply {
            part("grant_type", "refresh_token")
            part("client_id", clientId)
            part("client_secret", clientSecret)
            part("refresh_token", refreshToken)
        }
        return client.post()
            .uri("/api/oauth2/token")
            .body(builder.build())
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

