package com.inwonerplan.poc.aandachtspunten

import com.inwonerplan.poc.POCSubmissions
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandler
import com.ritense.valtimo.camunda.domain.CamundaTask

class OnAandachtsPuntenSubmittedEventHandler : FormViewModelSubmissionHandler<AandachtsPuntenViewModel> {

    override fun supports(formName: String): Boolean {
        return formName == "form_aandachtspunt"
    }

    override fun <T> handle(submission: T, task: CamundaTask) {
        println(submission)
        POCSubmissions.aandachtsPunten = submission as AandachtsPuntenViewModel
    }

}