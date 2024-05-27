package com.inwonerplan.poc.aanbod

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.ritense.formviewmodel.viewmodel.Submission


@JsonIgnoreProperties(ignoreUnknown = true)
data class AanbodSubmission(
    val aanbod: List<Aanbod>
) : Submission {
    data class Aanbod(
        val aandachtspunt: String,
        val subdoel: String,
        val aanbiedingen: List<Aanbieding>?
    )

    data class Aanbieding(
        val aanbod: String,
        val activiteit: String
    )
}