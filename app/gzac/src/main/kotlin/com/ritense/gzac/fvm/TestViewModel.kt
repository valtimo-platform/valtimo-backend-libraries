package com.ritense.gzac.fvm

import com.ritense.formviewmodel.viewmodel.Submission
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.valtimo.camunda.domain.CamundaTask

data class TestViewModel(
    val test: String
) : ViewModel, Submission {
    override fun update(task: CamundaTask?): ViewModel {
        return this
    }
}
