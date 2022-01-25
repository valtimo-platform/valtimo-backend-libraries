package com.ritense.valtimo.smartdocuments.domain

data class FilesResponse(
    val file: List<FileResponse>,
) {

    data class FileResponse(
        val filename: String,
        val document: DocumentResponse,
        val outputFormat: String,
    )

    data class DocumentResponse(
        val data: String,
    )
}
