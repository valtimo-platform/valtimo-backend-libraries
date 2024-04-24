package com.ritense.formviewmodel.web.rest

import com.ritense.formviewmodel.domain.ViewModel

data class TestViewModel(
    val test: String? = null
) : ViewModel {
    override fun update(viewModel: ViewModel): ViewModel {
        return this
    }
}