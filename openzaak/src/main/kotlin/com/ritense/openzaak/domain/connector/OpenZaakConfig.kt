package com.ritense.openzaak.domain.connector

import com.ritense.openzaak.domain.configuration.Rsin

data class OpenZaakConfig(
    var url: String = "",
    var clientId: String = "",
    val secret: String = "",
    var rsin: Rsin = Rsin("")
)