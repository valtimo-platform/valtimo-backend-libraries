package com.inwonerplan.poc.aanbod.command

import com.inwonerplan.poc.aanbod.AanbodSubmission
import com.ritense.formviewmodel.commandhandling.Command
import com.ritense.valtimo.camunda.domain.CamundaTask

data class SaveAanbodSubmissionCommand(
    val aanbodSubmission: AanbodSubmission,
    val task: CamundaTask
) : Command<Unit>