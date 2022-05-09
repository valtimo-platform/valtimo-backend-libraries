package com.ritense.valtimo.formflow.web.rest.result

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

class GetFormFlowStateResult(
    @JsonProperty(value = "id")
    val id: UUID,

    @JsonProperty
    val step: FormFlowStepResult
)