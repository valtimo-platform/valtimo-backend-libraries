package com.inwonerplan.poc.aanbod

import com.ritense.formviewmodel.event.FormViewModelSubmission
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandler


class OnAanbodSubmittedEventHandler : FormViewModelSubmissionHandler {

    override fun supports(formName: String): Boolean {
        return formName == "form_aanbod"
    }

    override fun handle(formViewModelSubmission: FormViewModelSubmission) {
        println(formViewModelSubmission)
    }

}