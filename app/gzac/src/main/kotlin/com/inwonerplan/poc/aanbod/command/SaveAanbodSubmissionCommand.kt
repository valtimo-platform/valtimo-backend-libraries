package com.inwonerplan.poc.aanbod.command

import com.inwonerplan.poc.aanbod.AanbodViewModel
import com.ritense.formviewmodel.commandhandling.Command
import com.ritense.valtimo.camunda.domain.CamundaTask

data class SaveAanbodSubmissionCommand(
    val aanbodSubmission: AanbodViewModel,
    val task: CamundaTask
) : Command<Unit>