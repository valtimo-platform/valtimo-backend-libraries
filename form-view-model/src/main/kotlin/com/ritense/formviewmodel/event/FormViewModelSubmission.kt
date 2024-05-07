package com.ritense.formviewmodel.event

import com.fasterxml.jackson.databind.node.ObjectNode

data class FormViewModelSubmission(
    val formName: String,
    val submission: ObjectNode,
    val taskInstanceId: String
)