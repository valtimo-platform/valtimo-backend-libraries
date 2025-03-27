package com.ritense.formviewmodel.submission

import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.viewmodel.TestViewModel
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.uicomponent.domain.UIComponentProcessLink
import com.ritense.valtimo.camunda.domain.CamundaTask

open class TestUserTaskUIComponentSubmissionHandler(
    val componentKey: String = "my-component",
) : FormViewModelUserTaskSubmissionHandler<TestViewModel> {

    override fun supports(processLink: ProcessLink): Boolean {
        return (processLink as? UIComponentProcessLink)?.componentKey == componentKey
    }

    override fun <T> handle(submission: T, task: CamundaTask, businessKey: String) {
        submission as TestViewModel

        if (submission.age!! < 18) {
            throw FormException("Age should be 18 or older")
        }
    }

}