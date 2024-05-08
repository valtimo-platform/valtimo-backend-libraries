package com.inwonerplan.poc.aandachtspunten

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.inwonerplan.model.Aandachtspunt
import com.ritense.formviewmodel.domain.ViewModel

@JsonIgnoreProperties(ignoreUnknown = true)
data class AandachtsPuntenViewModel(
    val aandachtspunten: List<Aandachtspunt>,
    val aandachtspunt: String?,
    val notitie: String?,
    val aandachtspuntenGrid: List<AandachtsPuntGridRow>?
) : ViewModel {
    override fun update(): ViewModel {
        return this
    }
}

data class AandachtsPuntGridRow(
    val aandachtspunt: String,
    val notitie: String
)