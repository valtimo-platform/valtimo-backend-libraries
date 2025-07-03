package com.ritense.formviewmodel.submission

import com.ritense.form.domain.FormProcessLink
import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.processlink.domain.ProcessLink
import com.ritense.valtimo.operaton.domain.OperatonTask
import java.util.UUID

open class TestUserTaskSubmissionHandler(
    val formName: String = "test",
    val formDefinitionId: UUID = UUID.randomUUID(),
) : FormViewModelUserTaskSubmissionHandler<TestViewModel> {

    override fun supports(processLink: ProcessLink): Boolean {
        return (processLink as? FormProcessLink)?.formDefinitionId == formDefinitionId
    }

    override fun supports(formName: String): Boolean {
        return formName == this.formName
    }

    override fun <T> handle(submission: T, task: OperatonTask, businessKey: String) {
        submission as TestViewModel

        if (submission.age!! < 18) {
            throw FormException("Age should be 18 or older")
        }
    }

}