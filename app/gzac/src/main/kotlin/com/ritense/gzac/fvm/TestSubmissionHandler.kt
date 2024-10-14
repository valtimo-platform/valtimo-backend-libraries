package com.ritense.gzac.fvm

import com.ritense.commandhandling.dispatchCommand
import com.ritense.formviewmodel.commandhandling.CompleteTaskCommand
import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.submission.FormViewModelUserTaskSubmissionHandler
import com.ritense.valtimo.camunda.domain.CamundaTask
import org.springframework.stereotype.Component

@Component
class TestSubmissionHandler : FormViewModelUserTaskSubmissionHandler<TestViewModel> {
    override fun supports(formName: String): Boolean = formName == "fvm-test"

    override fun <T> handle(submission: T, task: CamundaTask, businessKey: String) {
        throw FormException("This is a Business error", "test")
        dispatchCommand(
            CompleteTaskCommand(
                taskId = task.id
            )
        )
    }
}