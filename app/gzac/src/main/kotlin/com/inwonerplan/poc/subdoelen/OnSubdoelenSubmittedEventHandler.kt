package com.inwonerplan.poc.subdoelen

import com.inwonerplan.poc.POCSubmissions
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandler
import com.ritense.valtimo.camunda.domain.CamundaTask

class OnSubdoelenSubmittedEventHandler : FormViewModelSubmissionHandler<SubdoelenViewModel> {

    override fun supports(formName: String): Boolean {
        return formName == "form_subdoelen"
    }

    override fun <T> handle(submission: T, task: CamundaTask) {
        println(submission)
        POCSubmissions.subdoelen = submission as SubdoelenViewModel
    }

}