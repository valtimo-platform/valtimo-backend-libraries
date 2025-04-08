package com.ritense.documentenapi.bulk

data class DocumentImportStatus(
    val total: Number,
    val processed: Number,
    val processedSuccessfully: Number,
    val processedInvalid: Number,
    val status: String,
)