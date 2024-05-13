package com.ritense.formviewmodel.event

import com.ritense.formviewmodel.viewmodel.Submission
import com.ritense.valtimo.camunda.domain.CamundaTask
import kotlin.reflect.KClass

interface FormViewModelSubmissionHandler<T : Submission> {

    fun supports(formName: String): Boolean

    fun <T> handle(submission: T, task: CamundaTask)

    @Suppress("UNCHECKED_CAST")
    fun getSubmissionType(): KClass<T> = this::class.supertypes.first().arguments.first().type!!.classifier as KClass<T>

}