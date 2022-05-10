package com.ritense.valtimo.formflow.web.rest.result

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

class GetFormFlowStateResult(
    val id: UUID? = null,
    val step: FormFlowStepResult? = null,
    val errorMessage: String? = null
)
