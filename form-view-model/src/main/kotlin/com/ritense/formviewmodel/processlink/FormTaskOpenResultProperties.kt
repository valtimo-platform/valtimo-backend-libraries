package com.ritense.formviewmodel.processlink

import com.fasterxml.jackson.databind.JsonNode
import java.util.UUID

data class FormViewModelTaskOpenResultProperties(
    val formDefinitionId: UUID,
    val formDefinition: JsonNode,
    val formName: String
)