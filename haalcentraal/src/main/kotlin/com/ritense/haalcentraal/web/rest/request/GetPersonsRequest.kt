package com.ritense.haalcentraal.web.rest.request

data class GetPersonsRequest(
    val bsn: String?,
    val geslachtsnaam: String?,
    val geboortedatum: String?
)