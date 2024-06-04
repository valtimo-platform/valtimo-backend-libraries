package com.inwonerplan.poc.aandachtspunten

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.inwonerplan.model.Aandachtspunt
import com.ritense.formviewmodel.viewmodel.Submission
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.valtimo.camunda.domain.CamundaTask

@JsonIgnoreProperties(ignoreUnknown = true)
data class AandachtsPuntenViewModel(
    val aandachtspunten: List<Aandachtspunt>,
    val aandachtspuntenGrid: List<AandachtsPuntGridRow>?
) : ViewModel, Submission {
    override fun update(task: CamundaTask?): ViewModel {
        return this
    }
}

data class AandachtsPuntGridRow(
    val aandachtspunt: String,
    val notitie: String
)