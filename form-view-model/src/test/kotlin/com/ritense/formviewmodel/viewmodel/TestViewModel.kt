package com.ritense.formviewmodel.viewmodel

data class TestViewModel(
    val test: String? = null,
    val age: Int? = null,
    val dataContainer: TestData? = null,
) : ViewModel, Submission {
    override fun update(): ViewModel {
        return this
    }
}

data class TestData(
    val nestedData: String? = null,
)