package com.ritense.formviewmodel.event

class FormViewModelSubmissionHandlerFactory(
    private val formViewModelSubmissionHandlers: List<FormViewModelSubmissionHandler>
) {

    fun getFormViewModelSubmissionHandler(formName: String): FormViewModelSubmissionHandler? {
        return formViewModelSubmissionHandlers.find { it.supports(formName) }
    }

}