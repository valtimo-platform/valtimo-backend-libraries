package com.inwonerplan.poc.start

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.authorization.AuthorizationContext
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.commandhandling.dispatchCommand
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.formviewmodel.commandhandling.StartProcessCommand
import com.ritense.formviewmodel.submission.FormViewModelStartFormSubmissionHandler

class StartSubmissionHandler(
    private val jsonSchemaDocumentService: JsonSchemaDocumentService
) : FormViewModelStartFormSubmissionHandler<StartViewModel> {

    override fun <T> handle(
        documentDefinitionName: String,
        processDefinitionKey: String,
        submission: T
    ) {
        submission as StartViewModel
        val document = jsonSchemaDocumentService.createDocument(
            NewDocumentRequest(
                documentDefinitionName,
                jacksonObjectMapper().valueToTree(submission)
            )
        ).resultingDocument().orElseThrow()

        dispatchCommand(
            StartProcessCommand(
                caseInstanceId = document.id!!.id,
                processDefinitionKey = processDefinitionKey,
                businessKey = document.id!!.id.toString(),
            )
        )
    }

    override fun supports(formName: String): Boolean {
        return formName == "empty-form"
    }
}