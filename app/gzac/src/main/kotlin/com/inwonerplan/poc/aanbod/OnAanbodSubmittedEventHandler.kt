package com.inwonerplan.poc.aanbod

import com.inwonerplan.poc.aanbod.command.CompleteIntakeGesprekCommand
import com.inwonerplan.poc.model.Aanbieding
import com.inwonerplan.poc.model.Aanbod
import com.inwonerplan.poc.model.Inwonerplan
import com.ritense.commandhandling.dispatchCommands
import com.ritense.formviewmodel.commandhandling.CompleteTaskCommand
import com.ritense.formviewmodel.error.BusinessException
import com.ritense.formviewmodel.submission.FormViewModelUserTaskSubmissionHandler
import com.ritense.valtimo.camunda.domain.CamundaTask

class OnAanbodSubmittedEventHandler : FormViewModelUserTaskSubmissionHandler<AanbodSubmission> {

    override fun <T> handle(submission: T, task: CamundaTask, businessKey: String) {
        submission as AanbodSubmission
        val intakeGesprekSubmission = Inwonerplan(
            aanbod = submission.aanbodGrid.map {
                Aanbod(
                    aandachtspunt = it.aandachtspunt!!,
                    subdoel = it.subdoel!!,
                    aanbiedingen = it.aanbiedingenGrid?.map { aanbodRow ->
                        Aanbieding(
                            aanbod = aanbodRow.aanbod!!,
                            activiteit = aanbodRow.activiteit!!
                        )
                    }
                )
            }
        )

        try {
            dispatchCommands(listOf(
                CompleteIntakeGesprekCommand(intakeGesprekSubmission, task),
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