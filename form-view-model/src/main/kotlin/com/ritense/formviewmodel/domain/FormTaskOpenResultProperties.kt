package com.ritense.formviewmodel.domain

import com.fasterxml.jackson.databind.JsonNode
import java.util.UUID

data class FormViewModelTaskOpenResultProperties(
    val formDefinitionId: UUID,
    val formDefinition: JsonNode,
    val formName: String
)