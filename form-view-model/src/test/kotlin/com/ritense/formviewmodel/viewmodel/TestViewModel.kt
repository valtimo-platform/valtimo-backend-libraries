package com.ritense.formviewmodel.viewmodel

data class TestViewModel(
    val test: String? = null,
    val age: Int? = null
) : ViewModel, Submission {
    override fun update(): ViewModel {
        return this
    }
}