package com.ritense.valtimo.formflow.web.rest.result

import com.fasterxml.jackson.annotation.JsonProperty
import com.ritense.formflow.domain.definition.configuration.step.StepTypeProperties
import java.util.UUID

class FormFlowStepResult(
    val id: UUID,
    val type: String,
    val typeProperties: Any
)
