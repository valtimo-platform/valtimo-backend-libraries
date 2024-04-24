package com.ritense.formviewmodel.domain

interface ViewModelLoader<T : ViewModel> {
    fun onLoad(taskInstanceId: String): ViewModel

    fun supports(formId: String): Boolean
}