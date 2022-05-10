package com.ritense.valtimo.formflow.web.rest.result

import com.fasterxml.jackson.annotation.JsonProperty
import com.ritense.formflow.domain.definition.configuration.step.StepTypeProperties
import java.util.UUID

class FormFlowStepResult(
    @JsonProperty(value = "id")
    val id: UUID,
    @JsonProperty
    val type: String,
    @JsonProperty(value = "type-properties")
    val typeProperties: StepTypeProperties
)