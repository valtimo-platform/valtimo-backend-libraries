package com.ritense.formviewmodel.viewmodel

import com.ritense.valtimo.camunda.domain.CamundaTask

data class TestViewModel(
    val test: String? = null,
    val age: Int? = null,
    val dataContainer: TestData? = null,
    val reversedString: String? = null
) : ViewModel, Submission {

    override fun update(task: CamundaTask?, page: Int?): ViewModel {
        return this.copy(reversedString = reversedString?.reversed())
    }

    data class TestData(
        val nestedData: String? = null,
    )
}

