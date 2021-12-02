package com.ritense.mail.flowmailer.domain

data class OauthTokenResponse(
    val accessToken: String,
    val expiresIn: Int,
    val scope: String,
    val tokenType: String
)
