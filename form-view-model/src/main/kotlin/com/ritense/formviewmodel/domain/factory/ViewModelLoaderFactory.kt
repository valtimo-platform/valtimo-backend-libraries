package com.ritense.formviewmodel.domain.factory

import com.ritense.formviewmodel.domain.ViewModel
import com.ritense.formviewmodel.domain.ViewModelLoader
import kotlin.reflect.KClass

class ViewModelLoaderFactory {
    private val viewModelLoaders = mutableMapOf<KClass<*>, ViewModelLoader<ViewModel>>()

    fun getViewModelLoader(formId: String): ViewModelLoader<ViewModel>? {
        for (loader in viewModelLoaders.values) {
            if (loader.supports(formId)) {
                return loader
            }
        }
        return null
    }
}