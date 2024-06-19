package com.inwonerplan.poc.aanbod

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.inwonerplan.model.Aanbod
import com.inwonerplan.model.AanbodActiviteit
import com.inwonerplan.model.Subdoel
import com.ritense.formviewmodel.viewmodel.Submission


@JsonIgnoreProperties(ignoreUnknown = true)
data class AanbodSubmission(
    val aanbodGrid: List<AanbodGridRow>,
) : Submission {
    data class AanbodGridRow(
        val aandachtspunt: String?,
        val subdoel: String?,
        val aanbiedingenGrid: List<AanbodRow>?
    )

    data class AanbodRow(
        val aanbod: String?,
        val status: String?,
        val activiteit: String?
    )
}