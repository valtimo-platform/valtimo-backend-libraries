package com.inwonerplan.poc.subdoelen

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.inwonerplan.model.Subdoel
import com.ritense.formviewmodel.domain.ViewModel

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubdoelenViewModel(
    val aandachtspunt: String,
    val subdoelenGrid: List<SubdoelenGridRow>,
    val subdoelen: List<Subdoel>,
    val subdoel: String?
) : ViewModel {
    override fun update(): ViewModel {
        return this
    }
}

data class SubdoelenGridRow(
    val aandachtspunt: String,
    val subdoel: String? = null,
    val status: String? = null
)