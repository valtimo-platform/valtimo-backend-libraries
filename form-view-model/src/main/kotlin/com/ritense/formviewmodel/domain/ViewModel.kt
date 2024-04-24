package com.ritense.formviewmodel.domain

interface ViewModel {
    fun update(viewModel: ViewModel): ViewModel
}