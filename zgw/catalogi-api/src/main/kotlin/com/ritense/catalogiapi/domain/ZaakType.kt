package com.ritense.catalogiapi.domain

import java.net.URI

data class ZaakType (
    val url: URI,
    val omschrijving: String,
    val omschrijvingGeneriek: String? = null
)