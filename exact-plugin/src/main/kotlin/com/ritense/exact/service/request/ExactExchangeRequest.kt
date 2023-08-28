package com.ritense.exact.service.request

data class ExactExchangeRequest(
    val clientId: String,
    val clientSecret: String,
    val code: String,
)
