package com.ritense.formviewmodel.viewmodel

class ViewModelLoaderFactory(
    private val viewModelLoaders: List<ViewModelLoader<*>>
) {

    fun getViewModelLoader(formName: String): ViewModelLoader<out ViewModel>? {
        return viewModelLoaders.find { it.supports(formName) }
    }
}