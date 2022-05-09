package com.ritense.valtimo.formflow.web.rest.result

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

class GetFormFlowStateResult(
    @JsonProperty(value = "id")
    val id: UUID? = null,

    @JsonProperty
    val step: FormFlowStepResult? = null,

    @JsonProperty
    val errorMessage: String? = null
)