package com.ritense.formviewmodel.event

interface FormViewModelSubmissionHandler {

    fun supports(formName: String): Boolean

    fun handle(formViewModelSubmission: FormViewModelSubmission)

}