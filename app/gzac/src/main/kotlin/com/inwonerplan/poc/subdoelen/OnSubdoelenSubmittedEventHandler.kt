package com.inwonerplan.poc.subdoelen

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.inwonerplan.poc.POCSubmissions
import com.ritense.formviewmodel.event.FormViewModelSubmission
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandler

class OnSubdoelenSubmittedEventHandler : FormViewModelSubmissionHandler {

    override fun supports(formName: String): Boolean {
        return formName == "form_subdoelen"
    }

    override fun handle(formViewModelSubmission: FormViewModelSubmission) {
        println(formViewModelSubmission)
        POCSubmissions.subdoelen = jacksonObjectMapper().convertValue(formViewModelSubmission.submission, SubdoelenViewModel::class.java)
    }

}