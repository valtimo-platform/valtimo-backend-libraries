package com.ritense.formviewmodel.event

import com.ritense.formviewmodel.viewmodel.Submission

class FormViewModelSubmissionHandlerFactory(
    private val formViewModelSubmissionHandlers: List<FormViewModelSubmissionHandler<*>>
) {

    fun getFormViewModelSubmissionHandler(formName: String): FormViewModelSubmissionHandler<out Submission>? {
        return formViewModelSubmissionHandlers.find { it.supports(formName) }
    }

}