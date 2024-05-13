package com.inwonerplan.poc.aanbod

import com.ritense.formviewmodel.event.OnFormSubmittedEventHandler

class OnAanbodSubmittedEventHandler : OnFormSubmittedEventHandler<AanbodViewModel> {

    override fun supports(formName: String): Boolean {
        return formName == "form_aanbod"
    }

    override fun handle(submission: AanbodViewModel, taskInstanceId: String) {
        println(submission)
    }

}