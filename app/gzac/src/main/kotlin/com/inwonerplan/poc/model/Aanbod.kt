package com.inwonerplan.poc.model

data class Aanbod(
    val aandachtspunt: String,
    val subdoel: String,
    val aanbiedingen: List<Aanbieding>?
)
