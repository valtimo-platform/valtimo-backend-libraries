package com.ritense.formviewmodel.viewmodel

import com.ritense.valtimo.camunda.domain.CamundaTask

data class TestViewModel(
    val test: String? = null,
    val age: Int? = null,
    val dataContainer: TestData? = null,
) : ViewModel, Submission {

    override fun update(task: CamundaTask?): ViewModel {
        return this
    }

}

data class TestData(
    val nestedData: String? = null,
)