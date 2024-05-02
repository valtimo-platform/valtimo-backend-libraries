package com.ritense.formviewmodel.event

import com.ritense.formviewmodel.domain.ViewModel

interface OnFormSubmittedEventHandler<T : ViewModel> {

    fun supports(formId: String): Boolean
    fun handle(submission: T, taskInstanceId: String)

}