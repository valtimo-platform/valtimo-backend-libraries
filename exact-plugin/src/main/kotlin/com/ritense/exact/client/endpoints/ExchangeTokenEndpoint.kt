package com.ritense.exact.client.endpoints

import com.fasterxml.jackson.annotation.JsonProperty
import com.ritense.exact.client.endpoints.ExchangeTokenEndpoint.ExchangeTokenResponse
import com.ritense.exact.client.endpoints.structs.ExactEndpoint
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.web.client.RestClient

class ExchangeTokenEndpoint(
    private val redirectUrl: String,
    private val clientId: String,
    private val clientSecret: String,
    private val code: String
) : ExactEndpoint<ExchangeTokenResponse>(ExchangeTokenResponse::class.java) {

    override fun create(client: RestClient): RestClient.RequestHeadersSpec<*> {
        val builder = MultipartBodyBuilder().apply {
            part("grant_type", "authorization_code")
            part("client_id", clientId)
            part("client_secret", clientSecret)
            part("code", code)
            part("redirect_uri", redirectUrl)
        }
        return client
            .post()
            .uri("/api/oauth2/token")
            .body(builder.build())
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



