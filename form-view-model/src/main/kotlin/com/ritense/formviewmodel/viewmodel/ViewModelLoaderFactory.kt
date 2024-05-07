package com.ritense.formviewmodel.viewmodel

class ViewModelLoaderFactory(
    val viewModelLoaders: List<ViewModelLoader<*>>
) {

    fun getViewModelLoader(formId: String): ViewModelLoader<out ViewModel>? {
        for (loader in viewModelLoaders) {
            if (loader.supports(formId)) {
                return loader
            }
        }
        return null
    }
}