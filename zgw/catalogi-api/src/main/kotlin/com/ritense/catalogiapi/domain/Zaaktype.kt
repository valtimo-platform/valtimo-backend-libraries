package com.ritense.catalogiapi.domain

import java.net.URI

data class Zaaktype (
    val url: URI,
    val omschrijving: String,
    val omschrijvingGeneriek: String? = null
)