package com.ritense.haalcentraal.domain

data class Person(
    val burgerservicenummer: String,
    val voornamen: String?,
    val voorletters: String?,
    val geslachtsnaam: String?,
    val geboorteDatum: String?
)