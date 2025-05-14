package com.ritense.processdocument.domain

data class UpdateProcessDefinitionCaseDefinitionRequest(
    val canInitializeDocument: Boolean? = null,
    val startableByUser: Boolean? = null
)