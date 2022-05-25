package com.ritense.formflow.domain.definition.configuration

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.ritense.formflow.domain.definition.configuration.step.StepTypeProperties

data class FormFlowStepType(
    val name: String,

    @JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "name")
    val properties: StepTypeProperties
)