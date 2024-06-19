package com.inwonerplan.poc.aanbod.command

import com.inwonerplan.poc.model.Inwonerplan
import com.ritense.commandhandling.Command
import com.ritense.valtimo.camunda.domain.CamundaTask

data class CompleteIntakeGesprekCommand(
    val intakeGesprekSubmission: Inwonerplan,
    val task: CamundaTask
) : Command<Unit>