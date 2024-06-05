package com.inwonerplan.poc.aanbod

import com.inwonerplan.poc.aanbod.command.SaveAanbodSubmissionCommand
import com.ritense.commandhandling.dispatchCommands
import com.ritense.formviewmodel.commandhandling.CompleteTaskCommand
import com.ritense.formviewmodel.error.BusinessException
import com.ritense.formviewmodel.submission.FormViewModelUserTaskSubmissionHandler
import com.ritense.valtimo.camunda.domain.CamundaTask

class OnAanbodSubmittedEventHandler : FormViewModelUserTaskSubmissionHandler<AanbodViewModel> {

    override fun <T> handle(submission: T, task: CamundaTask, businessKey: String) {
        submission as AanbodViewModel
        val aanbodSubmission = AanbodSubmission(
            aanbod = submission.aanbodGrid.map {
                AanbodSubmission.Aanbod(
                    aandachtspunt = it.aandachtspunt!!,
                    subdoel = it.subdoel!!,
                    aanbiedingen = it.aanbiedingenGrid?.map { aanbodRow ->
                        AanbodSubmission.Aanbieding(
                            aanbod = aanbodRow.aanbod!!,
                            activiteit = aanbodRow.activiteit!!
                        )
                    }
                )
            }
        )

        try {
            dispatchCommands(listOf(
                SaveAanbodSubmissionCommand(aanbodSubmission, task),
                CompleteTaskCommand(task.id)
            ))
        } catch(e: BusinessException) {
            throw e
        } catch(e: Exception) {
            throw BusinessException("Could not save document")
        }
    }

    override fun supports(formName: String): Boolean {
        return formName == "form_aanbod"
    }
}