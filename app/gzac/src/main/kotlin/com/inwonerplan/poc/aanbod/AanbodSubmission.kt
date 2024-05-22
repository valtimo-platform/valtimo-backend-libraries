package com.inwonerplan.poc.aanbod

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.ritense.formviewmodel.viewmodel.Submission


@JsonIgnoreProperties(ignoreUnknown = true)
data class AanbodSubmission(
    val aanbod: String
) : Submission