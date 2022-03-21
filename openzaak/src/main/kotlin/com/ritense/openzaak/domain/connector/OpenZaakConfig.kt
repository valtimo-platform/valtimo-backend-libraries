package com.ritense.openzaak.domain.connector

import com.ritense.openzaak.domain.configuration.Rsin

data class OpenZaakConfig(
    var url: String = "",
    var clientId: String = "",
    var secret: String = "",
    var rsin: Rsin = Rsin(""),
    var catalogUrl: String = "",
)