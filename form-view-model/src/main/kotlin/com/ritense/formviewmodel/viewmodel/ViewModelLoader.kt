package com.ritense.formviewmodel.viewmodel

import kotlin.reflect.KClass

interface ViewModelLoader<T : ViewModel> {

    fun load(taskInstanceId: String): T

    fun supports(formName: String) = getFormName() == formName

    @Suppress("UNCHECKED_CAST")
    fun getViewModelType() : KClass<T> = this::class.supertypes.first().arguments.first().type!!.classifier as KClass<T>

    fun getFormName(): String

}