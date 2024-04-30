package com.ritense.formviewmodel.domain.factory

import com.ritense.formviewmodel.domain.ViewModel
import com.ritense.formviewmodel.domain.ViewModelLoader
import kotlin.reflect.KClass

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