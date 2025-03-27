package com.ritense.formviewmodel.submission

import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.form.domain.FormProcessLink
import com.ritense.form.service.FormDefinitionService
import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.formviewmodel.web.rest.dto.StartFormSubmissionResult
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

open class TestStartFormSubmissionHandler(
    val userTaskFormName: String = "test",
    val startFormName: String = "test",
    val newDocumentId: UUID = UUID.randomUUID(),
    val formDefinitionService: FormDefinitionService
) : FormViewModelStartFormSubmissionHandler<TestViewModel> {

    override fun supports(processLink: ProcessLink): Boolean {
        val formProcessLink = processLink as? FormProcessLink?: return false
        val formName = formDefinitionService.getFormDefinitionById(formProcessLink.formDefinitionId).getOrNull()?.name

        return (formProcessLink.activityType == ActivityTypeWithEventName.USER_TASK_CREATE && formName == userTaskFormName) ||
            (formProcessLink.activityType == ActivityTypeWithEventName.START_EVENT_START && formName == startFormName)
    }

    override fun supports(formName: String): Boolean {
        return formName == this.userTaskFormName || formName == this.startFormName
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