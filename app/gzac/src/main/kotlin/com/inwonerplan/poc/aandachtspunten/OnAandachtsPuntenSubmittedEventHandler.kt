package com.inwonerplan.poc.aandachtspunten

import com.inwonerplan.poc.POCSubmissions
import com.ritense.formviewmodel.event.OnFormSubmittedEventHandler

class OnAandachtsPuntenSubmittedEventHandler : OnFormSubmittedEventHandler<AandachtsPuntenViewModel> {

    override fun supports(formName: String): Boolean {
        return formName == "form_aandachtspunt"
    }

    override fun handle(submission: AandachtsPuntenViewModel, taskInstanceId: String) {
        println(submission)
        POCSubmissions.aandachtsPunten = submission
    }

}