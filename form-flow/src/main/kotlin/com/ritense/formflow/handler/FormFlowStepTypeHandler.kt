package com.ritense.formflow.handler

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.formflow.domain.instance.FormFlowStepInstance

interface FormFlowStepTypeHandler {

    fun getType(): String

    fun getMetadata(stepInstance: FormFlowStepInstance): JsonNode

}
