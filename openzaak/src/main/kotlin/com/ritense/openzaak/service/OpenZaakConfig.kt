package com.ritense.openzaak.service

data class OpenZaakConfig(
    var url: String = "",
    var clientId: String = "",
    var secret: String = "",
    var rsin: String = ""
)