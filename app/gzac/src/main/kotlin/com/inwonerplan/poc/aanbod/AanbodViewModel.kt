package com.inwonerplan.poc.aanbod

import com.inwonerplan.model.Aanbod
import com.ritense.formviewmodel.domain.ViewModel

data class AanbodViewModel(
    val aandachtspunt: String,
    val subdoel: String,
    val aanbiedingen: List<Aanbod>,
    val aanbod: Aanbod?,
    val aanbodGrid: List<AanbodGridRow>
) : ViewModel {

    override fun update(): ViewModel {
        return this
    }
}

data class AanbodGridRow(
    val aandachtspunt: String,
    val subdoel: String,
    val aanbiedingen: List<AanbodRow>?
)

data class AanbodRow(
    val aanbod: String,
    val status: String
)