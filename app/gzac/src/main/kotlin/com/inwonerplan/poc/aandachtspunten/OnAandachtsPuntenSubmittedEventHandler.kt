package com.inwonerplan.poc.aandachtspunten

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.inwonerplan.poc.POCSubmissions
import com.ritense.formviewmodel.event.FormViewModelSubmission
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandler

class OnAandachtsPuntenSubmittedEventHandler : FormViewModelSubmissionHandler {

    override fun supports(formName: String): Boolean {
        return formName == "form_aandachtspunt"
    }

    override fun handle(formViewModelSubmission: FormViewModelSubmission) {
        println(formViewModelSubmission)
        POCSubmissions.aandachtsPunten = jacksonObjectMapper().convertValue(formViewModelSubmission.submission, AandachtsPuntenViewModel::class.java)
    }

}