package com.ritense.form.web.rest.dto

import com.fasterxml.jackson.databind.node.ObjectNode

data class IntermediateSaveRequest(
    val submission: ObjectNode,
    val taskInstanceId: String
)