package com.ritense.formviewmodel.submission

import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.formviewmodel.web.rest.dto.StartFormSubmissionResult
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.uicomponent.domain.UIComponentProcessLink
import java.util.UUID

open class TestStartFormUIComponentSubmissionHandler(
    val componentKey: String = "my-component",
    val newDocumentId: UUID = UUID.randomUUID(),
) : FormViewModelStartFormSubmissionHandler<TestViewModel> {

    override fun supports(processLink: ProcessLink): Boolean {
        return (processLink as? UIComponentProcessLink)?.componentKey == componentKey
    }

    override fun <T> handle(
        documentDefinitionName: String,
        processDefinitionKey: String,
        submission: T,
        document: JsonSchemaDocument?
    ): StartFormSubmissionResult {
        submission as TestViewModel
        if (submission.age!! < 18) {
            throw FormException("Age should be 18 or older")
        }

        return StartFormSubmissionResult(documentId = document?.id?.id ?: newDocumentId)
    }

}