package com.ritense.formviewmodel.event

import com.ritense.formviewmodel.viewmodel.Submission
import kotlin.reflect.KClass

interface FormViewModelSubmissionHandler<T : Submission> {

    fun supports(formName: String): Boolean

    fun handle(submission: T)

    @Suppress("UNCHECKED_CAST")
    fun getSubmissionType() = this::class.supertypes.first().arguments.first().type!!.classifier as KClass<T>

}