package com.ritense.documentenapi.bulk

data class CreateImportResult(
    val uploadUrl: String,
    val statusUrl: String,
    val reportUrl: String,
)