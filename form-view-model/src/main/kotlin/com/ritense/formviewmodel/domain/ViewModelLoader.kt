package com.ritense.formviewmodel.domain

import kotlin.reflect.KClass

interface ViewModelLoader<T : ViewModel> {
    fun onLoad(taskInstanceId: String): T

    fun supports(formName: String): Boolean

    @Suppress("UNCHECKED_CAST")
    fun getViewModelType() = this::class.supertypes.first().arguments.first().type!!.classifier as KClass<T>

    fun getFormName(): String
}