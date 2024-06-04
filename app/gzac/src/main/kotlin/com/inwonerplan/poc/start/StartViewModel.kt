package com.inwonerplan.poc.start

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.ritense.formviewmodel.viewmodel.Submission
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.valtimo.camunda.domain.CamundaTask

@JsonIgnoreProperties(ignoreUnknown = true)
data class StartViewModel(
    val test: String
) : ViewModel, Submission {

    override fun update(task: CamundaTask): ViewModel {
        return this
    }
}