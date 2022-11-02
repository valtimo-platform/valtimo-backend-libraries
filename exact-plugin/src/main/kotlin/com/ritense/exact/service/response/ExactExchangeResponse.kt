package com.ritense.exact.service.response

import java.time.LocalDateTime

data class ExactExchangeResponse(
    val accessToken: String,
    val accessTokenExpiresOn: LocalDateTime,
    val refreshToken: String,
    val refreshTokenExpiresOn: LocalDateTime
)