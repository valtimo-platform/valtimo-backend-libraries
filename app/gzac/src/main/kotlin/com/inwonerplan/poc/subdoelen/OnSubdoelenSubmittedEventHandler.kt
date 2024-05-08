package com.inwonerplan.poc.subdoelen

import com.inwonerplan.poc.POCSubmissions
import com.ritense.formviewmodel.event.OnFormSubmittedEventHandler

class OnSubdoelenSubmittedEventHandler : OnFormSubmittedEventHandler<SubdoelenViewModel> {

    override fun supports(formName: String): Boolean {
        return formName == "form_subdoelen"
    }

    override fun handle(submission: SubdoelenViewModel, taskInstanceId: String) {
        println(submission)
        POCSubmissions.subdoelen = submission
    }

}