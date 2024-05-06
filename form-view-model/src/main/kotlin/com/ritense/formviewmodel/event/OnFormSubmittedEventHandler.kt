package com.ritense.formviewmodel.event

import com.ritense.formviewmodel.domain.ViewModel

interface OnFormSubmittedEventHandler<T : ViewModel> {

    fun supports(formName: String): Boolean
    fun handle(submission: T, taskInstanceId: String)

}